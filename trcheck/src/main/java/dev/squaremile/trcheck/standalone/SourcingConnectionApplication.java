package dev.squaremile.trcheck.standalone;

import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.CommandFailed;
import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.DataSent;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.support.transport.ThingsOnDutyRunner;
import dev.squaremile.transport.aerontcpgateway.api.AeronConnection;
import dev.squaremile.transport.aerontcpgateway.api.AeronTcpGateway;
import dev.squaremile.transport.aerontcpgateway.api.AeronTcpGatewayClient;
import dev.squaremile.trcheck.probe.Measurements;
import dev.squaremile.trcheck.probe.Probe;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import static dev.squaremile.asynctcp.api.serialization.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;
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
        final ApplicationOnDuty source = createApplication(configuration.mode(), transport -> new ConnectingApplication(
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

    private static ApplicationOnDuty createApplication(final TcpPingConfiguration.Mode mode, final TransportApplicationOnDutyFactory applicationFactory)
    {
        final TransportApplicationFactory asyncTcp = new AsyncTcp();
        switch (mode)
        {
            case SHARED_STACK:
            {
                System.out.println("Creating an app without ring buffers");
                return asyncTcp.createSharedStack(
                        "source",
                        applicationFactory
                );
            }
            case RING_BUFFERS:
            {
                System.out.println("Creating an app that uses ring buffers");
                return asyncTcp.create(
                        "source",
                        1024 * 1024,
                        NO_OP,
                        applicationFactory
                );
            }
            case AERON:
                System.out.println("Creating an app that uses aeron");
                final MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED).dirDeleteOnShutdown(true));
                final AeronTcpGateway gateway = new AeronTcpGateway(new AeronConnection(10, 11, mediaDriver.aeronDirectoryName()));
                final AeronTcpGatewayClient gatewayClient = new AeronTcpGatewayClient(new AeronConnection(10, 11, mediaDriver.aeronDirectoryName()));
                gateway.connect();
                gatewayClient.connect();
                runUntil(new ThingsOnDutyRunner(gatewayClient, gateway).reached(() -> gatewayClient.isConnected() && gateway.isConnected()));
                System.out.println("Connected to Aeron Gateway");
                final TransportApplicationOnDuty gatewayClientApplication = gatewayClient.create(
                        "source",
                        applicationFactory,
                        SerializedEventListener.NO_OP
                );
                return new TransportApplicationOnDuty()
                {
                    @Override
                    public void onStart()
                    {
                        gatewayClientApplication.onStart();
                    }

                    @Override
                    public void onStop()
                    {
                        gatewayClientApplication.onStop();
                    }

                    @Override
                    public void work()
                    {
                        gateway.work();
                        gatewayClient.work();
                        gatewayClientApplication.work();
                    }

                    @Override
                    public void close()
                    {
                        gatewayClientApplication.close();
                        gatewayClient.close();
                        gateway.close();
                    }

                    @Override
                    public void onEvent(final Event event)
                    {
                        gatewayClientApplication.onEvent(event);
                    }
                };
            default:
                throw new IllegalArgumentException(mode.name());
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
        if (event instanceof DataSent)
        {
            DataSent dataSent = (DataSent)event;
            probe.onDataSent(dataSent.totalBytesSent(), nanoTime());
        }
    }
}
