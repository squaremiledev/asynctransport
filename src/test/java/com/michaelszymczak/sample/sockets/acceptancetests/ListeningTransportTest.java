package com.michaelszymczak.sample.sockets.acceptancetests;

import java.net.ConnectException;

import com.michaelszymczak.sample.sockets.domain.api.commands.Listen;
import com.michaelszymczak.sample.sockets.domain.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.domain.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.domain.api.events.NumberOfConnectionsChanged;
import com.michaelszymczak.sample.sockets.domain.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.domain.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.support.SampleClient;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;


class ListeningTransportTest extends TransportTestBase
{
    @Test
    void shouldAcceptConnections()
    {
        final int port = freePort();

        // Given
        transport.handle(transport.command(Listen.class).set(102, port));
        transport.workUntil(() -> transport.events().contains(StartedListening.class));
        transport.events().last(StartedListening.class);
        assertEqual(transport.events().all(StartedListening.class), new StartedListening(port, 102));

        // When
        transport.workUntil(completed(() -> clients.client(1).connectedTo(port)));

        // Then
        transport.workUntil(() -> !transport.connectionEvents().all(ConnectionAccepted.class).isEmpty());
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).isNotEmpty();
        ConnectionAccepted event = transport.connectionEvents().last(ConnectionAccepted.class);
        assertThat(event.port()).isEqualTo(port);
        assertThat(event.commandId()).isEqualTo(102);
    }

    @Test
    @Tag("tcperror")
    void shouldNotAcceptIfNotAsked()
    {
        assertThrows(ConnectException.class, () -> clients.client(1).connectedTo(freePort()));
    }

    @Test
    @Tag("tcperror")
    void shouldStopListeningWhenAsked()
    {
        transport.handle(transport.command(Listen.class).set(0, freePort()));
        transport.workUntil(() -> transport.events().contains(StartedListening.class));
        final int port = transport.events().last(StartedListening.class).port();

        // When
        transport.handle(transport.command(StopListening.class).set(9, port));
        transport.workUntil(() -> transport.events().contains(StoppedListening.class));

        // Then
        assertEqual(transport.events().all(StoppedListening.class), new StoppedListening(port, 9));
        assertThrows(ConnectException.class, () -> clients.client(1).connectedTo(port));
    }

    @Test
    void shouldIgnoreStopListeningCommandForNonExistingRequest()
    {
        // Given
        final int port = freePort();
        final int anotherPort = freePortOtherThan(port);
        transport.handle(transport.command(Listen.class).set(2, port));
        transport.workUntil(() -> transport.events().contains(StartedListening.class));

        // When
        transport.handle(transport.command(StopListening.class).set(4, anotherPort));
        transport.workUntil(() -> transport.events().contains(TransportCommandFailed.class));

        // Then
        final TransportCommandFailed event = transport.events().last(TransportCommandFailed.class);
        assertThat(event.commandId()).isEqualTo(4);
        assertThat(event.port()).isEqualTo(anotherPort);
        clients.client(1).connectedTo(port);
    }

    @Test
    void shouldBeAbleToListenOnMoreThanOnePort()
    {
        // When
        final int serverPort1 = freePort();
        transport.handle(transport.command(Listen.class).set(0, serverPort1));
        final int serverPort2 = freePortOtherThan(serverPort1);
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 1);

        // When
        transport.handle(transport.command(Listen.class).set(1, serverPort2));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 2);

        // Then
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).isEmpty();
        clients.client(1).connectedTo(serverPort1);
        clients.client(2).connectedTo(serverPort2);
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 2);
    }

    @Test
    void shouldBeAbleToListenOnTheSamePortAgain()
    {
        // When
        final int serverPort1 = freePort();
        transport.handle(transport.command(Listen.class).set(101, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 1);
        transport.handle(transport.command(StopListening.class).set(102, serverPort1));
        transport.workUntil(() -> transport.events().all(StoppedListening.class).size() == 1);

        // When
        transport.handle(transport.command(Listen.class).set(103, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 2);

        // Then
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).isEmpty();
        clients.client(1).connectedTo(serverPort1);
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 1);
    }

    @Test
    void shouldBeAbleToListenOnTheSameAfterStoppedListeningWhileConnectionIsUp()
    {
        // When
        final int serverPort1 = freePort();
        transport.handle(transport.command(Listen.class).set(101, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 1);
        clients.client(1).connectedTo(serverPort1);
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 1);
        transport.handle(transport.command(StopListening.class).set(102, serverPort1));
        transport.workUntil(() -> transport.events().all(StoppedListening.class).size() == 1);

        // When
        transport.handle(transport.command(Listen.class).set(103, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 2);

        // Then
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSize(1);
        clients.client(2).connectedTo(serverPort1);
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 2);
    }

    @Test
    void shouldBeAbleToListenOnTheSameAfterStoppedListeningAndClosedConnection()
    {
        // When
        final int serverPort1 = freePort();
        transport.handle(transport.command(Listen.class).set(101, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 1);
        clients.client(1).connectedTo(serverPort1);
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 1);
        transport.handle(transport.command(StopListening.class).set(102, serverPort1));
        transport.workUntil(() -> transport.events().all(StoppedListening.class).size() == 1);
        clients.client(1).close();
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 2);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);

        // When
        transport.handle(transport.command(Listen.class).set(103, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 2);

        // Then
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSize(2);
        clients.client(2).connectedTo(serverPort1);
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 3);
    }


    @Test
    void shouldRejectWhenAskedToListenOnTheSamePortAtTheSameTime()
    {
        // When
        final int serverPort1 = freePort();
        transport.handle(transport.command(Listen.class).set(101, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 1);
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSize(0);

        // When
        transport.handle(transport.command(Listen.class).set(102, serverPort1));

        // Then
        transport.workUntil(() -> transport.events().all(CommandFailed.class).size() == 1);
        assertThat(transport.events().all(StartedListening.class)).hasSize(1);
        assertEqual(transport.events().all(CommandFailed.class), new TransportCommandFailed(serverPort1, 102, "Address already in use"));
        clients.client(1).connectedTo(serverPort1);
        transport.workUntil(() -> transport.statusEvents().all(NumberOfConnectionsChanged.class).size() == 1);
    }

    @Test
    void shouldRejectWhenAskedImmediatelyToListenOnTheSamePort()
    {
        // When
        final int serverPort1 = freePort();
        transport.handle(transport.command(Listen.class).set(101, serverPort1));
        transport.handle(transport.command(Listen.class).set(102, serverPort1));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 1);

        // Then
        transport.workUntil(() -> transport.events().all(CommandFailed.class).size() == 1);
        assertThat(transport.events().all(StartedListening.class)).hasSize(1);
        assertEqual(transport.events().all(CommandFailed.class), new TransportCommandFailed(serverPort1, 102, "Address already in use"));
    }


    @Test
    @Tag("tcperror")
    void shouldUseRequestIdToFindThePortItShouldStopListeningOn()
    {
        // Given
        final int port1 = freePort();
        final int port2 = freePortOtherThan(port1);
        final int port3 = freePortOtherThan(port1, port2);
        assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port1));
        assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port2));
        assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port3));

        transport.handle(transport.command(Listen.class).set(5, port1));
        transport.handle(transport.command(Listen.class).set(6, port2));
        transport.handle(transport.command(Listen.class).set(7, port3));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 3);
        assertEqual(transport.events().all(StartedListening.class), new StartedListening(port1, 5), new StartedListening(port2, 6), new StartedListening(port3, 7));

        // When
        transport.handle(transport.command(StopListening.class).set(9, port2));
        transport.workUntil(() -> transport.events().contains(StoppedListening.class));

        // Then
        assertThat(transport.events().last(StoppedListening.class).commandId()).isEqualTo(9);
        assertThat(transport.events().last(StoppedListening.class).port()).isEqualTo(port2);
        clients.client(1).connectedTo(port1);
        assertThrows(ConnectException.class, () -> clients.client(2).connectedTo(port2));
        clients.client(3).connectedTo(port3);
    }
}
