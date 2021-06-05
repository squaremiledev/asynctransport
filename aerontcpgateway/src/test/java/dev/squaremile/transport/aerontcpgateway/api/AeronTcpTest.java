package dev.squaremile.transport.aerontcpgateway.api;

import org.agrona.collections.MutableInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.support.transport.FreePort;
import dev.squaremile.asynctcp.support.transport.ThingsOnDutyRunner;
import dev.squaremile.transport.aerontcpgateway.FakeServer;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.api.serialization.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;

class AeronTcpTest
{
    private final int port = FreePort.freePort();
    private final TransportApplicationOnDuty fakeServer = FakeServer.startFakeServerListeningOn(port);

    @AfterEach
    void tearDown()
    {
        fakeServer.close();
    }

    @Test
    void shouldUseAeronToEstablishTcpConnection()
    {
        final MutableInteger portOnWhichTCPConnectionHasBeenEstablished = new MutableInteger();
        final TransportApplicationOnDutyFactory tcpConnectionInitiator = transport -> new TransportApplicationOnDuty()
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
                    portOnWhichTCPConnectionHasBeenEstablished.set(((Connected)event).remotePort());
                }
            }
        };

        try (final TransportApplicationOnDuty tcpApplication = new AeronTcp().createInProcess("role", NO_OP, tcpConnectionInitiator))
        {
            tcpApplication.onStart();
            assertThat(portOnWhichTCPConnectionHasBeenEstablished.value).isEqualTo(0);
            runUntil(new ThingsOnDutyRunner(tcpApplication).reached(() -> portOnWhichTCPConnectionHasBeenEstablished.value != 0));
            assertThat(portOnWhichTCPConnectionHasBeenEstablished.value).isEqualTo(port);
        }
    }
}