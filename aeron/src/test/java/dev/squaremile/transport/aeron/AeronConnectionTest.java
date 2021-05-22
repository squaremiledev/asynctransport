package dev.squaremile.transport.aeron;

import org.agrona.collections.MutableBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.events.Connected;
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

    private final int port = FreePort.freePort();
    private MediaDriver mediaDriver;
    private TransportApplicationOnDuty fakeServer;

    @BeforeEach
    void setUp()
    {
        mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED).dirDeleteOnShutdown(true));
        fakeServer = FakeServer.startFakeServerListeningOn(port);
    }

    @AfterEach
    void tearDown()
    {
        fakeServer.close();
        mediaDriver.close();
    }

    @Test
    void tcpSandbox()
    {
        final MutableBoolean hasConnected = new MutableBoolean(false);

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

        TransportApplicationOnDuty role = new AsyncTcp().createWithoutTransport(
                "app <-> aeron",
                transport -> new TransportApplicationOnDuty()
                {
                    @Override
                    public void onStart()
                    {
                        transport.handle(transport.command(Connect.class).set("localhost", port, 2, 5_000, fixedLengthDelineation(Integer.BYTES)));
                    }

                    @Override
                    public void onEvent(final Event event)
                    {
                        if (event instanceof Connected)
                        {
                            Connected connected = (Connected)event;
                            if (connected.remotePort() == port)
                            {
                                hasConnected.set(true);
                            }
                        }
                    }
                },
                new AeronBackedEventSupplier(networkToUserSubscription),
                new AeronSerializedCommandPublisher(userToNetworkPublication),
                SerializedEventListener.NO_OP
        );

        TransportOnDuty transport = new AsyncTcp().createTransport(
                "aeron <-> tcp",
                new AeronBackedCommandSupplier(userToNetworkSubscription),
                new AeronSerializedEventPublisher(networkToUserPublication)
        );

        assertThat(hasConnected.value).isFalse();
        ThingsOnDutyRunner thingsOnDutyRunner = new ThingsOnDutyRunner(fakeServer, role, transport);
        role.onStart();
        runUntil(thingsOnDutyRunner.reached(hasConnected::get));
        assertThat(hasConnected.value).isTrue();

        aeronUserInstance.close();
        aeronNetworkInstance.close();
    }
}