package com.michaelszymczak.sample.sockets.acceptancetests;

import java.net.SocketException;
import java.util.List;

import com.michaelszymczak.sample.sockets.domain.api.CommandId;
import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;
import com.michaelszymczak.sample.sockets.domain.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.domain.api.commands.Listen;
import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionResetByPeer;
import com.michaelszymczak.sample.sockets.domain.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.domain.api.events.DataSent;
import com.michaelszymczak.sample.sockets.domain.api.events.NumberOfConnectionsChanged;
import com.michaelszymczak.sample.sockets.domain.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.support.TransportDriver;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static java.nio.charset.StandardCharsets.US_ASCII;


class ConnectingTransportTest extends TransportTestBase
{
    @Test
    void shouldNotifyWhenConnected()
    {
        // Given
        transport.handle(transport.command(Listen.class).set(1, freePort()));
        final int serverPort = transport.events().last(StartedListening.class).port();
        assertThat(transport.statusEvents().contains(NumberOfConnectionsChanged.class)).isFalse();


        // When
        final int clientPort = freePort();
        transport.workUntil(completed(() -> clients.client(1).connectedTo(serverPort, clientPort)));
        transport.workUntil(() -> transport.events().contains(ConnectionAccepted.class));

        // Then
        assertEqual(transport.statusEvents().all(NumberOfConnectionsChanged.class), new NumberOfConnectionsChanged(1));
        assertThat(transport.events().all(ConnectionAccepted.class)).hasSize(1);
        final ConnectionAccepted connectionAcceptedEvent = transport.events().last(ConnectionAccepted.class);
        assertThat(connectionAcceptedEvent).usingRecursiveComparison().isEqualTo(
                new ConnectionAccepted(
                        serverPort,
                        1,
                        clientPort,
                        connectionAcceptedEvent.connectionId(),
                        connectionAcceptedEvent.maxInboundMessageSize(),
                        connectionAcceptedEvent.maxOutboundMessageSize()
                ));
    }

    @Test
    void shouldProvideConnectionDetailsForEachConnection()
    {
        // Given
        transport.handle(transport.command(Listen.class).set(5, freePort()));
        final int serverPort = transport.events().last(StartedListening.class).port();

        // When
        transport.workUntil(completed(() -> clients.client(1).connectedTo(serverPort)));
        transport.workUntil(completed(() -> clients.client(2).connectedTo(serverPort)));
        transport.workUntil(() -> transport.events().all(ConnectionAccepted.class).size() >= 2);

        // Then
        final List<ConnectionAccepted> events = transport.events().all(ConnectionAccepted.class);
        assertThat(events).hasSize(2);
        assertThat(events.get(0).commandId()).isEqualTo(events.get(1).commandId());
        assertThat(events.get(0).port()).isEqualTo(events.get(1).port());
        assertThat(events.get(0).remotePort()).isNotEqualTo(events.get(1).remotePort());
        assertThat(events.get(0).connectionId()).isNotEqualTo(events.get(1).connectionId());
        assertEqual(transport.statusEvents().all(NumberOfConnectionsChanged.class), new NumberOfConnectionsChanged(1), new NumberOfConnectionsChanged(2));
    }

    @Test
    void shouldCloseConnection()
    {
        // Given
        transport.handle(transport.command(Listen.class).set(9, freePort()));
        transport.workUntil(() -> !transport.events().all(StartedListening.class).isEmpty());
        final int serverPort = transport.events().last(StartedListening.class).port();
        assertThrows(SocketException.class, clients.client(1)::write); // throws if not connected when writing
        transport.workUntil(completed(() -> clients.client(1).connectedTo(serverPort)));
        transport.workUntil(() -> !transport.events().all(ConnectionAccepted.class).isEmpty());
        final ConnectionAccepted connectionAccepted = transport.events().last(ConnectionAccepted.class);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);

        // When
        transport.handle(transport.command(CloseConnection.class).set(connectionAccepted, 10));

        // Then
        assertThat(clients.client(1).hasServerClosedConnection()).isTrue();
        assertThat(transport.events().last(ConnectionClosed.class)).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(connectionAccepted.port(), connectionAccepted.connectionId(), 10));
        assertThat(transport.events().all(ConnectionClosed.class)).hasSize(1);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }

    @Test
    void shouldCloseConnectionOnce()
    {
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);
        transport.handle(transport.command(CloseConnection.class).set(conn, 15));
        assertThat(transport.events().last(ConnectionClosed.class)).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(conn.port(), conn.connectionId(), 15));
        assertThat(transport.events().all(ConnectionClosed.class)).hasSize(1);
        assertThat(transport.events().all(TransportCommandFailed.class)).isEmpty();
        assertThat(clients.client(1).hasServerClosedConnection()).isTrue();
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSize(2);

        // When
        transport.handle(transport.command(CloseConnection.class).set(conn, 16));

        // Then
        assertThat(transport.events().last(TransportCommandFailed.class).commandId()).isEqualTo(16);
        assertThat(transport.events().all(ConnectionClosed.class)).hasSize(1);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSize(2);
    }

    @Test
    void shouldRejectClosingNonExistingConnection()
    {
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).isEmpty();

        // When
        transport.handle(transport.command(CloseConnection.class).set(1234, 11111, 15));

        // Then
        assertThat(transport.events().last(TransportCommandFailed.class, event -> event.commandId() == 15).details()).containsIgnoringCase("connection id");
        assertThat(transport.events().last(TransportCommandFailed.class, event -> event.commandId() == 15).port()).isEqualTo(1234);
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).isEmpty();
    }

    @Test
    void shouldNotifyWhenConnectedWhileListeningOnMultiplePorts()
    {
        // Given
        final int listeningPort1 = freePort();
        final int listeningPort2 = freePortOtherThan(listeningPort1);
        transport.handle(transport.command(Listen.class).set(5, listeningPort1));
        transport.handle(transport.command(Listen.class).set(6, listeningPort2));
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 5).port()).isEqualTo(listeningPort1);
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 6).port()).isEqualTo(listeningPort2);

//        // When
        final int clientPort1 = freePortOtherThan(listeningPort1, listeningPort2);
        final int clientPort2 = freePortOtherThan(listeningPort1, listeningPort2, clientPort1);
        transport.workUntil(completed(() -> clients.client(1).connectedTo(listeningPort1, clientPort1)));
        transport.workUntil(completed(() -> clients.client(2).connectedTo(listeningPort2, clientPort2)));

        // Then
        transport.workUntil(() -> transport.events().all(ConnectionAccepted.class).size() >= 2);
        final ConnectionAccepted connectionAccepted1 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort1);
        final ConnectionAccepted connectionAccepted2 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort2);
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(2);

        assertThat(connectionAccepted1.connectionId()).isNotEqualTo(connectionAccepted2.connectionId());
        assertThat(connectionAccepted1.commandId()).isEqualTo(5);
        assertThat(connectionAccepted2.commandId()).isEqualTo(6);
        assertThat(connectionAccepted1.port()).isEqualTo(listeningPort1);
        assertThat(connectionAccepted2.port()).isEqualTo(listeningPort2);
        assertThat(connectionAccepted1.remotePort()).isNotEqualTo(connectionAccepted2.remotePort());
    }

    @Test
    void shouldNotifyWhenStartedListeningAndConnectedTwice()
    {
        // Given
        final int listeningPort1 = freePort();
        final int listeningPort2 = freePortOtherThan(listeningPort1);
        final int clientPort1 = freePortOtherThan(listeningPort1, listeningPort2);
        final int clientPort2 = freePortOtherThan(listeningPort1, listeningPort2, clientPort1);

        // When
        transport.handle(transport.command(Listen.class).set(5, listeningPort1));
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 5).port()).isEqualTo(listeningPort1);
        transport.workUntil(completed(() -> clients.client(1).connectedTo(listeningPort1, clientPort1)));
        transport.handle(transport.command(Listen.class).set(6, listeningPort2));
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 6).port()).isEqualTo(listeningPort2);
        transport.workUntil(completed(() -> clients.client(2).connectedTo(listeningPort2, clientPort2)));

        // Then
        transport.workUntil(() -> transport.events().all(ConnectionAccepted.class).size() >= 2);
        final ConnectionAccepted connectionAccepted1 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort1);
        final ConnectionAccepted connectionAccepted2 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort2);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(2);

        assertThat(connectionAccepted1.connectionId()).isNotEqualTo(connectionAccepted2.connectionId());
        assertThat(connectionAccepted1.commandId()).isEqualTo(5);
        assertThat(connectionAccepted2.commandId()).isEqualTo(6);
        assertThat(connectionAccepted1.port()).isEqualTo(listeningPort1);
        assertThat(connectionAccepted2.port()).isEqualTo(listeningPort2);
        assertThat(connectionAccepted1.remotePort()).isNotEqualTo(connectionAccepted2.remotePort());
    }

    @Test
    void shouldNotifyWhenRemoteEndpointImmediatelyClosedConnection()
    {
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(1);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);

        // When
        clients.client(1).close();
        transport.workUntil(() -> transport.events().contains(ConnectionClosed.class));

        // Then
        final ConnectionClosed connectionClosed = transport.connectionEvents().last(ConnectionClosed.class, conn.connectionId());
        assertThat(connectionClosed).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(conn.port(), conn.connectionId(), CommandId.NO_COMMAND_ID));
        assertThat(transport.events().contains(DataReceived.class)).isFalse();
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }

    @Test
    void shouldNotifyWhenRemoteEndpointEventuallyClosedConnection()
    {
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionId connection = driver.listenAndConnect(clients.client(1));
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(1);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);
        driver.successfullySendToClient(connection, clients.client(1), "foo");
        final DataSent lastDataSent = transport.connectionEvents().last(DataSent.class, connection.connectionId());

        // When
        clients.client(1).close();
        transport.workUntil(() -> transport.events().contains(ConnectionClosed.class));

        // Then
        final ConnectionClosed connectionClosed = transport.connectionEvents().last(ConnectionClosed.class, connection.connectionId());
        assertThat(connectionClosed).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(connection.port(), connection.connectionId(), CommandId.NO_COMMAND_ID));
        assertThat(transport.events().contains(DataReceived.class)).isFalse();
        assertThat(transport.connectionEvents().last(DataSent.class, connection.connectionId())).usingRecursiveComparison()
                .isEqualTo(lastDataSent);
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }

    @Test
    @Tag("tcperror")
    void shouldInformedThatConnectionResetByPeer()
    {
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        conn.port();
        conn.connectionId();
        transport.handle(transport.command(conn, SendData.class).set("foo".getBytes(US_ASCII)));
        conn.port();
        conn.connectionId();
        transport.handle(transport.command(conn, SendData.class).set("BA".getBytes(US_ASCII)));
        transport.workUntil(() -> transport.events().all(DataSent.class).size() == 2);

        //When
        clients.client(1).close();
        transport.workUntil(() -> transport.events().contains(ConnectionResetByPeer.class));
        transport.workTimes(10);

        // Then
        assertEqual(
                transport.events().all(ConnectionResetByPeer.class),
                new ConnectionResetByPeer(conn.port(), conn.connectionId(), CommandId.NO_COMMAND_ID)
        );
        assertThat(transport.events().contains(ConnectionClosed.class)).isFalse();
        assertThat(transport.events().contains(DataReceived.class)).isFalse();
        assertThat(transport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(transport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }
}
