package dev.squaremile.trcheck.standalone;

import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.CommandFailed;
import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.trcheck.probe.Measurements;
import dev.squaremile.trcheck.probe.Probe;

import static dev.squaremile.asynctcp.api.serialization.SerializedMessageListener.NO_OP;
import static dev.squaremile.trcheck.probe.Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH;
import static java.lang.System.nanoTime;

class SourcingConnectionApplication implements ConnectionApplication
{
    private final Probe probe;
    private final ConnectionTransport connectionTransport;
    private final MutableBoolean isDone;
    private final byte[] extraData;

    public SourcingConnectionApplication(
            final Probe probe,
            final ConnectionTransport connectionTransport,
            final MutableBoolean isDone,
            final int extraDataLength
    )
    {
        this.probe = probe;
        this.connectionTransport = connectionTransport;
        this.isDone = isDone;
        this.extraData = generateExtraData(extraDataLength);
    }

    public static Measurements runPing(final TcpPingConfiguration configuration)
    {
        final Probe probe = configuration.probeConfig().createProbe();

        final MutableBoolean isDone = new MutableBoolean(false);
        final ApplicationOnDuty source = createApplication(configuration.useBuffers(), transport -> new ConnectingApplication(
                transport,
                configuration.remoteHost(),
                configuration.remotePort(),
                new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 0, ""),
                (connectionTransport, connectionId) ->
                        new SourcingConnectionApplication(
                                probe,
                                connectionTransport,
                                isDone,
                                configuration.extraDataLength()
                        )
        ));

        source.onStart();
        while (!isDone.get())
        {
            source.work();
        }
        source.onStop();

        return probe.measurementsCopy();
    }

    private static ApplicationOnDuty createApplication(final boolean useBuffers, final TransportApplicationOnDutyFactory applicationFactory)
    {
        final TransportApplicationFactory asyncTcp = new AsyncTcp();
        if (useBuffers)
        {
            System.out.println("Creating an app that uses ring buffers");
            return asyncTcp.create(
                    "source",
                    1024 * 1024,
                    NO_OP,
                    applicationFactory
            );
        }
        else
        {
            System.out.println("Creating an app without ring buffers");
            return asyncTcp.createSharedStack(
                    "source",
                    applicationFactory
            );
        }
    }

    private static byte[] generateExtraData(final int extraDataLength)
    {
        byte[] data = new byte[extraDataLength];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = (byte)(i % 128);
        }
        return data;
    }

    @Override
    public void onStart()
    {
    }

    @Override
    public void work()
    {
        final SendMessage message = connectionTransport.command(SendMessage.class);
        final MutableDirectBuffer outboundBuffer = message.prepare();
        boolean anythingToSend = probe.onTime(nanoTime(), outboundBuffer, message.offset(), ALL_METADATA_FIELDS_TOTAL_LENGTH);
        if (anythingToSend)
        {
            outboundBuffer.putBytes(message.offset() + ALL_METADATA_FIELDS_TOTAL_LENGTH, extraData);
            message.commit(ALL_METADATA_FIELDS_TOTAL_LENGTH + extraData.length);
            connectionTransport.handle(message);
        }
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof CommandFailed)
        {
            throw new IllegalStateException(((CommandFailed)event).details());
        }

        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            probe.onMessageReceived(messageReceived.buffer(), messageReceived.offset(), nanoTime());
            if (probe.hasReceivedAll())
            {
                isDone.set(true);
            }
        }
    }
}
