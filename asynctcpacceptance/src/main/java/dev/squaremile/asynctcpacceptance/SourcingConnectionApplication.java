package dev.squaremile.asynctcpacceptance;

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
import dev.squaremile.tcpprobe.Measurements;
import dev.squaremile.tcpprobe.Probe;

import static dev.squaremile.asynctcp.api.serialization.SerializedMessageListener.NO_OP;
import static dev.squaremile.tcpprobe.Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH;
import static dev.squaremile.tcpprobe.Metadata.DEFAULT_CORRELATION_ID_OFFSET;
import static dev.squaremile.tcpprobe.Metadata.DEFAULT_OPTIONS_OFFSET;
import static dev.squaremile.tcpprobe.Metadata.DEFAULT_SEND_TIME_OFFSET;
import static dev.squaremile.tcpprobe.Probe.probe;
import static java.lang.Integer.parseInt;
import static java.lang.System.nanoTime;

public class SourcingConnectionApplication implements ConnectionApplication
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

    private static byte[] generateExtraData(final int extraDataLength)
    {
        byte[] data = new byte[extraDataLength];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = (byte)(i % 128);
        }
        return data;
    }

    public static void main(String[] args)
    {
        if (args.length != 8)
        {
            System.out.println("Usage: remoteHost remotePort sendingRatePerSecond skippedWarmUpResponses messagesSent respondOnlyToNthRequest useBuffers extraDataLength");
            System.out.println("e.g. localhost 8889 48000 400000 4000000 1 0 0");
            System.out.println("e.g. localhost 8889 48000 400000 4000000 2 1 32");
            return;
        }
        String remoteHost = args[0];
        final int remotePort = parseInt(args[1]);
        final int sendingRatePerSecond = parseInt(args[2]);
        final int skippedWarmUpResponses = parseInt(args[3]);
        final int messagesSent = parseInt(args[4]);
        final int respondToEveryNthRequest = parseInt(args[5]);
        final boolean useBuffers = parseInt(args[6]) == 1;
        final int extraDataLength = parseInt(args[7]);

        final int expectedResponses = messagesSent / respondToEveryNthRequest;
        final String description = String.format(
                "remoteHost %s, remotePort %d, sendingRatePerSecond %d, skippedWarmUpResponses %d , " +
                "messagesSent %d, %d expected responses with a response rate 1 for %d, use buffers: %s, extra data %d bytes",
                remoteHost, remotePort, sendingRatePerSecond, skippedWarmUpResponses,
                messagesSent, expectedResponses, respondToEveryNthRequest, useBuffers, extraDataLength
        );
        System.out.println("Starting with " + description);
        Measurements measurements = start(
                remoteHost,
                remotePort,
                useBuffers,
                extraDataLength,
                probe(description)
                        .totalNumberOfMessagesToSend(messagesSent)
                        .skippedResponses(skippedWarmUpResponses)
                        .respondToEveryNthRequest(respondToEveryNthRequest)
                        .sendingRatePerSecond(sendingRatePerSecond)
                        .metadataOffsets(DEFAULT_OPTIONS_OFFSET, DEFAULT_SEND_TIME_OFFSET, DEFAULT_CORRELATION_ID_OFFSET)
        );
        measurements.printResults();
    }

    public static Measurements start(
            final String remoteHost,
            final int remotePort,
            final boolean useBuffers,
            final int extraDataLength,
            final Probe.Configuration configuration
    )
    {
        final Probe probe = configuration.createProbe();

        final MutableBoolean isDone = new MutableBoolean(false);
        final ApplicationOnDuty source = createApplication(useBuffers, transport -> new ConnectingApplication(
                transport,
                remoteHost,
                remotePort,
                new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 0, ""),
                (connectionTransport, connectionId) ->
                        new SourcingConnectionApplication(
                                probe,
                                connectionTransport,
                                isDone,
                                extraDataLength
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

    @Override
    public void onStart()
    {
    }

    @Override
    public void work()
    {
        final SendMessage message = connectionTransport.command(SendMessage.class);
        final MutableDirectBuffer outboundBuffer = message.prepare();
        int encodedLength = probe.onTime(nanoTime(), outboundBuffer, message.offset(), ALL_METADATA_FIELDS_TOTAL_LENGTH);
        if (encodedLength > 0)
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
