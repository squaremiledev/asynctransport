package dev.squaremile.asynctcpacceptance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.CommandFailed;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.api.transport.events.TransportCommandFailed;
import dev.squaremile.asynctcp.api.transport.values.TransportId;
import dev.squaremile.asynctcp.internal.transport.domain.NumberOfConnectionsChanged;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.support.transport.FreePort.freePort;
import static dev.squaremile.asynctcp.fixtures.transport.TearDown.closeCleanly;
import static dev.squaremile.asynctcpacceptance.Assertions.assertEqual;
import static java.util.Collections.singletonList;


class ClientConnectsTest extends TransportTestBase
{
    @ParameterizedTest
    @ValueSource(strings = {"localhost", "127.0.0.1"})
    void shouldConnect(final String remoteHost)
    {
        // Given
        TransportDriver serverDriver = new TransportDriver(serverTransport);
        StartedListening serverStartedListening = serverDriver.startListening();

        // When
        clientTransport.handle(clientTransport.command(Connect.class).set(remoteHost, serverStartedListening.port(), 101, 1_000, rawStreaming()));
        spinUntil(() -> !serverTransport.connectionEvents().all(ConnectionAccepted.class).isEmpty() &&
                        !clientTransport.connectionEvents().all(Connected.class).isEmpty());

        // Then
        ConnectionAccepted connectionAcceptedByServer = serverTransport.connectionEvents().last(ConnectionAccepted.class);
        Connected connected = clientTransport.events().last(Connected.class);
        assertEqual(
                clientTransport.events().all(Connected.class),
                new Connected(
                        connectionAcceptedByServer.remotePort(),
                        101,
                        remoteHost,
                        serverStartedListening.port(),
                        connected.connectionId(),
                        connected.inboundPduLimit(),
                        connected.outboundPduLimit(),
                        rawStreaming()
                )
        );
        assertEqual(clientTransport.statusEvents().all(), singletonList(new NumberOfConnectionsChanged(1)));
        assertEqual(serverTransport.statusEvents().all(), singletonList(new NumberOfConnectionsChanged(1)));
        assertThat(clientTransport.events().all(CommandFailed.class)).isEmpty();
    }

    @Test
    void shouldInformAboutFailedConnectionAttemptWhenConnectingToPortNothingListensOn()
    {
        int portNothingListensOn = freePort();

        // When
        clientTransport.handle(clientTransport.command(Connect.class).set("localhost", portNothingListensOn, 102, 1_000, rawStreaming()));
        spinUntilFailure();

        // Then
        assertEqual(
                clientTransport.events().all(CommandFailed.class),
                new TransportCommandFailed(
                        TransportId.NO_PORT,
                        102,
                        "Connection refused",
                        Connect.class
                )
        );
    }

    @Test
    void shouldInformAboutFailedConnectionAttemptWhenConnectingToNonExistingHost()
    {
        // Given
        TransportDriver serverDriver = new TransportDriver(serverTransport);
        StartedListening serverStartedListening = serverDriver.startListening();

        // When
        clientTransport.handle(clientTransport.command(Connect.class).set("240.0.0.0", serverStartedListening.port(), 101, 50, rawStreaming()));
        spinUntilFailure();

        // Then
        assertEqual(
                clientTransport.events().all(CommandFailed.class),
                new TransportCommandFailed(
                        TransportId.NO_PORT,
                        101,
                        "Timed out",
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
