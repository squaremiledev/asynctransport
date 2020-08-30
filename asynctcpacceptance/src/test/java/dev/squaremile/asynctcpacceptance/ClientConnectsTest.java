package dev.squaremile.asynctcpacceptance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.TransportId;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.events.CommandFailed;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.NumberOfConnectionsChanged;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.domain.api.events.TransportCommandFailed;

import static dev.squaremile.asynctcp.testfitures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;
import static dev.squaremile.asynctcp.testfitures.TearDown.closeCleanly;
import static java.util.Collections.singletonList;


class ClientConnectsTest extends TransportTestBase
{
    @Test
    void shouldConnect()
    {
        // Given
        TransportDriver serverDriver = new TransportDriver(serverTransport);
        StartedListening serverStartedListening = serverDriver.startListening();

        // When
        clientTransport.handle(clientTransport.command(Connect.class).set(serverStartedListening.port(), 101));
        spinUntil(() -> !serverTransport.connectionEvents().all(ConnectionAccepted.class).isEmpty() &&
                        !clientTransport.connectionEvents().all(Connected.class).isEmpty());

        // Then
        ConnectionAccepted connectionAcceptedByServer = serverTransport.connectionEvents().last(ConnectionAccepted.class);
        assertThat(clientTransport.events().all(CommandFailed.class)).isEmpty();
        Connected connected = clientTransport.events().last(Connected.class);
        assertEqual(
                clientTransport.events().all(Connected.class),
                new Connected(
                        connectionAcceptedByServer.remotePort(),
                        101,
                        serverStartedListening.port(),
                        connected.connectionId(),
                        connected.inboundPduLimit(),
                        connected.outboundPduLimit()
                )
        );
        assertEqual(clientTransport.statusEvents().all(), singletonList(new NumberOfConnectionsChanged(1)));
        assertEqual(serverTransport.statusEvents().all(), singletonList(new NumberOfConnectionsChanged(1)));
        assertThat(clientTransport.events().all(CommandFailed.class)).isEmpty();
    }

    @Test
    void shouldInformAboutFailedConnectionAttempt()
    {
        int portNothingListensOn = freePort();

        // When
        clientTransport.handle(clientTransport.command(Connect.class).set(portNothingListensOn, 101));
        spinUntilFailure();

        // Then
        assertEqual(
                clientTransport.events().all(CommandFailed.class),
                new TransportCommandFailed(
                        TransportId.NO_PORT,
                        101,
                        "Connection refused",
                        Connect.class
                )
        );
    }

    @AfterEach
    void tearDown()
    {
        closeCleanly(serverTransport);
        closeCleanly(clientTransport);
    }
}
