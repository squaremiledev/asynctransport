package dev.squaremile.transport.aeron;

import org.agrona.collections.MutableBoolean;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.internal.ApplicationWithThingsOnDuty;
import dev.squaremile.asynctcp.internal.serialization.messaging.MessageHandler;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedCommandSupplier;
import dev.squaremile.asynctcp.support.transport.FreePort;
import dev.squaremile.asynctcp.support.transport.ThingsOnDutyRunner;
import io.aeron.Aeron;
import io.aeron.ExclusivePublication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;

class AeronConnectionTest
{

    private MediaDriver mediaDriver;

    @BeforeEach
    void setUp()
    {
        mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED).dirDeleteOnShutdown(true));
    }

    @AfterEach
    void tearDown()
    {
        mediaDriver.close();
    }

    @Test
    void sandbox()
    {
        final AeronConnection aeronConnection = new AeronConnection(10, 11, mediaDriver.aeronDirectoryName());
        try (Aeron aeron = Aeron.connect(aeronConnection.aeronContext()))
        {
            Subscription toNetworkSubscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
            Subscription fromNetworkSubscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

            ExclusivePublication toNetworkPublication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
            ExclusivePublication fromNetworkPublication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

            while (!toNetworkPublication.isConnected())
            {

            }
            while (!fromNetworkPublication.isConnected())
            {

            }
        }
    }

    @Test
    void tcpSandbox()
    {
        final int port = FreePort.freePort();
        AsyncTcp asyncTcp = new AsyncTcp();
        MutableBoolean testerStartedListening = new MutableBoolean(false);
        TransportApplicationOnDuty tester = asyncTcp.createSharedStack("tester", transport -> new TransportApplicationOnDuty()
        {
            @Override
            public void onStart()
            {
                transport.handle(transport.command(Listen.class).set(1, port, fixedLengthDelineation(Integer.BYTES)));
            }

            @Override
            public void onEvent(final Event event)
            {
                System.out.println(event);
                if (event instanceof StartedListening)
                {
                    StartedListening startedListening = (StartedListening)event;
                    if (startedListening.port() == port)
                    {
                        testerStartedListening.set(true);
                    }
                }
            }
        });

        MutableBoolean sutConnected = new MutableBoolean(false);
        final TransportApplicationOnDutyFactory applicationFactory = transport -> new TransportApplicationOnDuty()
        {
            @Override
            public void onStart()
            {
                transport.handle(transport.command(Connect.class).set("localhost", port, 2, 5_000, fixedLengthDelineation(Integer.BYTES)));
            }

            @Override
            public void onEvent(final Event event)
            {
                System.out.println(event);
                if (event instanceof Connected)
                {
                    Connected connected = (Connected)event;
                    if (connected.remotePort() == port)
                    {
                        sutConnected.set(true);
                    }
                }
            }
        };

        final AeronConnection aeronConnection = new AeronConnection(10, 11, mediaDriver.aeronDirectoryName());
        Aeron aeronNetworkInstance = Aeron.connect(aeronConnection.aeronContext());
        Aeron aeronUserInstance = Aeron.connect(aeronConnection.aeronContext());

        Subscription userToNetworkSubscription = aeronNetworkInstance.addSubscription(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
        Subscription networkToUserSubscription = aeronUserInstance.addSubscription(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

        ExclusivePublication userToNetworkPublication = aeronUserInstance.addExclusivePublication(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
        ExclusivePublication networkToUserPublication = aeronNetworkInstance.addExclusivePublication(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

        while (!userToNetworkPublication.isConnected())
        {

        }
        while (!networkToUserPublication.isConnected())
        {

        }


        final RingBuffer networkToUser = new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 + TRAILER_LENGTH]));
        final RingBuffer userToNetwork = new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 + TRAILER_LENGTH]));
        ApplicationWithThingsOnDuty applicationWithThingsOnDuty = new ApplicationWithThingsOnDuty(
                asyncTcp.createWithoutTransport(
                        "role",
                        applicationFactory,
                        networkToUser::read,
                        (sourceBuffer, sourceOffset, length) ->
                        {
                            boolean success = userToNetwork.write(1, sourceBuffer, sourceOffset, length);
                            if (!success)
                            {
                                throw new IllegalStateException("Unable to write to the buffer");
                            }
                        },
                        (sourceBuffer, sourceOffset, length) ->
                        {

                        }
                ),
                asyncTcp.createTransport(
                        "role:transport",
                        new SerializedCommandSupplier()
                        {
                            @Override
                            public int poll(final MessageHandler handler)
                            {
                                userToNetworkSubscription.poll((buffer, offset, length, header) -> handler.onMessage(buffer, offset, length), 10);
                                return userToNetwork.read(handler);
                            }
                        },
                        (sourceBuffer, sourceOffset, length) -> networkToUser.write(1, sourceBuffer, sourceOffset, length)
                )
        );


        ThingsOnDutyRunner thingsOnDutyRunner = new ThingsOnDutyRunner(tester, applicationWithThingsOnDuty);
        tester.onStart();
        runUntil(thingsOnDutyRunner.reached(testerStartedListening::get));
        applicationWithThingsOnDuty.onStart();
        runUntil(thingsOnDutyRunner.reached(sutConnected::get));


        aeronUserInstance.close();
        aeronNetworkInstance.close();
    }
}