package dev.squaremile.transport.aerontcpgateway.api;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.support.transport.FreePort;
import dev.squaremile.asynctcp.support.transport.ThingsOnDutyRunner;
import dev.squaremile.transport.aerontcpgateway.FakeServer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.api.serialization.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;

class AeronTcpTest
{
    private final int port = FreePort.freePort();
    private final TransportApplicationOnDuty fakeServer = FakeServer.startFakeServerListeningOn(port);
    private final TcpConnectionInitiator tcpConnectionInitiator = new TcpConnectionInitiator();

    @AfterEach
    void tearDown()
    {
        fakeServer.close();
    }

    @Test
    void shouldProvideClientWithDriverEmbedded()
    {
        try (final TransportApplicationOnDuty tcpApplication = new AeronTcp().create("role", NO_OP, tcpConnectionInitiator))
        {
            tcpApplication.onStart();
            assertThat(tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished()).isEmpty();
            runUntil(new ThingsOnDutyRunner(tcpApplication).reached(() -> tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished().isPresent()));
            assertThat(tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished().get()).isEqualTo(port);
        }
    }

    @Test
    void shouldProvideSeparateTcpDriver()
    {
        try (
                final TcpDriver tcpDriver = new AeronTcp().createEmbeddedTcpDriver(10, 11).start();
                final TransportApplicationOnDuty tcpApplication = new AeronTcp().create("role", NO_OP, tcpConnectionInitiator, tcpDriver.configuration())
        )
        {
            tcpApplication.onStart();
            assertThat(tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished()).isEmpty();
            runUntil(new ThingsOnDutyRunner(tcpApplication, tcpDriver).reached(() -> tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished().isPresent()));
            assertThat(tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished().get()).isEqualTo(port);
        }
    }

    @Test
    void shouldUtilizeExistingMediaDriver()
    {
        final TcpConnectionInitiator tcpConnectionInitiator = new TcpConnectionInitiator();
        try (
                final MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED).dirDeleteOnShutdown(true));
                final TcpDriver tcpDriver = new AeronTcp().createTcpDriver(10, 11, mediaDriver.aeronDirectoryName()).start();
                final TransportApplicationOnDuty tcpApplication = new AeronTcp().create("role", NO_OP, tcpConnectionInitiator, tcpDriver.configuration())
        )
        {

            tcpApplication.onStart();
            assertThat(tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished()).isEmpty();
            runUntil(new ThingsOnDutyRunner(tcpApplication, tcpDriver).reached(() -> tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished().isPresent()));
            assertThat(tcpConnectionInitiator.portOnWhichTCPConnectionHasBeenEstablished().get()).isEqualTo(port);
        }
    }

    private class TcpConnectionInitiator implements TransportApplicationOnDutyFactory
    {
        private Integer portOnWhichTCPConnectionHasBeenEstablished = null;

        public Optional<Integer> portOnWhichTCPConnectionHasBeenEstablished()
        {
            return Optional.ofNullable(portOnWhichTCPConnectionHasBeenEstablished);
        }

        @Override
        public TransportApplicationOnDuty create(final Transport transport)
        {
            return new TransportApplicationOnDuty()
            {
                @Override
                public void onStart()
                {
                    transport.handle(transport.command(Connect.class).set("localhost", port, 2, 5_000, rawStreaming()));
                }

                @Override
                public void onEvent(final Event event)
                {
                    if (event instanceof Connected)
                    {
                        portOnWhichTCPConnectionHasBeenEstablished = ((Connected)event).remotePort();
                    }
                }
            };
        }
    }
}