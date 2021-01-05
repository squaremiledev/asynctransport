package dev.squaremile.asynctcpacceptance;

import java.net.SocketException;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.internal.domain.NumberOfConnectionsChanged;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.transport.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePortOtherThan;
import static java.nio.charset.StandardCharsets.US_ASCII;


class ServerReceivesConnectionsTest extends TransportTestBase
{
    @Test
    void shouldNotifyWhenConnected()
    {
        // Given
        serverTransport.handle(serverTransport.command(Listen.class).set(1, freePort(), rawStreaming()));
        final int serverPort = serverTransport.events().last(StartedListening.class).port();
        assertThat(serverTransport.statusEvents().contains(NumberOfConnectionsChanged.class)).isFalse();


        // When
        final int clientPort = freePort();
        serverTransport.workUntil(completed(() -> clients.client(1).connectedTo(serverPort, clientPort)));
        serverTransport.workUntil(() -> serverTransport.events().contains(ConnectionAccepted.class));

        // Then
        assertEqual(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class), new NumberOfConnectionsChanged(1));
        assertThat(serverTransport.events().all(ConnectionAccepted.class)).hasSize(1);
        final ConnectionAccepted connectionAcceptedEvent = serverTransport.events().last(ConnectionAccepted.class);
        assertThat(connectionAcceptedEvent).usingRecursiveComparison().isEqualTo(
                new ConnectionAccepted(
                        serverPort,
                        1,
                        "localhost",
                        clientPort,
                        connectionAcceptedEvent.connectionId(),
                        connectionAcceptedEvent.inboundPduLimit(),
                        connectionAcceptedEvent.outboundPduLimit(),
                        rawStreaming()
                ));
    }

    @Test
    void shouldProvideConnectionDetailsForEachConnection()
    {
        // Given
        serverTransport.handle(serverTransport.command(Listen.class).set(5, freePort(), rawStreaming()));
        final int serverPort = serverTransport.events().last(StartedListening.class).port();

        // When
        serverTransport.workUntil(completed(() -> clients.client(1).connectedTo(serverPort)));
        serverTransport.workUntil(completed(() -> clients.client(2).connectedTo(serverPort)));
        serverTransport.workUntil(() -> serverTransport.events().all(ConnectionAccepted.class).size() >= 2);

        // Then
        final List<ConnectionAccepted> events = serverTransport.events().all(ConnectionAccepted.class);
        assertThat(events).hasSize(2);
        assertThat(events.get(0).commandId()).isEqualTo(events.get(1).commandId());
        assertThat(events.get(0).port()).isEqualTo(events.get(1).port());
        assertThat(events.get(0).remotePort()).isNotEqualTo(events.get(1).remotePort());
        assertThat(events.get(0).connectionId()).isNotEqualTo(events.get(1).connectionId());
        assertEqual(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class), new NumberOfConnectionsChanged(1), new NumberOfConnectionsChanged(2));
    }

    @Test
    void shouldCloseConnection()
    {
        // Given
        serverTransport.handle(serverTransport.command(Listen.class).set(9, freePort(), rawStreaming()));
        serverTransport.workUntil(() -> !serverTransport.events().all(StartedListening.class).isEmpty());
        final int serverPort = serverTransport.events().last(StartedListening.class).port();
        assertThrows(SocketException.class, clients.client(1)::write); // throws if not connected when writing
        serverTransport.workUntil(completed(() -> clients.client(1).connectedTo(serverPort)));
        serverTransport.workUntil(() -> !serverTransport.events().all(ConnectionAccepted.class).isEmpty());
        final ConnectionAccepted connectionAccepted = serverTransport.events().last(ConnectionAccepted.class);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);

        // When
        serverTransport.handle(serverTransport.command(connectionAccepted.connectionId(), CloseConnection.class).set(10));

        // Then
        assertThat(clients.client(1).hasServerClosedConnection()).isTrue();
        assertThat(serverTransport.events().last(ConnectionClosed.class)).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(connectionAccepted.port(), connectionAccepted.connectionId(), 10));
        assertThat(serverTransport.events().all(ConnectionClosed.class)).hasSize(1);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }

    @Test
    void shouldCloseConnectionOnce()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);
        serverTransport.handle(serverTransport.command(conn.connectionId(), CloseConnection.class).set(15));
        assertThat(serverTransport.events().last(ConnectionClosed.class)).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(conn.port(), conn.connectionId(), 15));
        assertThat(serverTransport.events().all(ConnectionClosed.class)).hasSize(1);
        assertThat(serverTransport.events().all(TransportCommandFailed.class)).isEmpty();
        assertThat(clients.client(1).hasServerClosedConnection()).isTrue();
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSize(2);

        // When
        serverTransport.handle(new CloseConnection(conn).set(16));

        // Then
        assertThat(serverTransport.events().last(TransportCommandFailed.class).commandId()).isEqualTo(16);
        assertThat(serverTransport.events().all(ConnectionClosed.class)).hasSize(1);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSize(2);
    }

    @Test
    void shouldRejectClosingNonExistingConnection()
    {
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).isEmpty();

        // When
        serverTransport.handle(new CloseConnection(new ConnectionIdValue(1234, 11111)).set(15));

        // Then
        assertThat(serverTransport.events().last(TransportCommandFailed.class, event -> event.commandId() == 15).details()).containsIgnoringCase("connection id");
        assertThat(serverTransport.events().last(TransportCommandFailed.class, event -> event.commandId() == 15).port()).isEqualTo(1234);
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).isEmpty();
    }

    @Test
    void shouldNotifyWhenConnectedWhileListeningOnMultiplePorts()
    {
        // Given
        final int listeningPort1 = freePort();
        final int listeningPort2 = freePortOtherThan(listeningPort1);
        serverTransport.handle(serverTransport.command(Listen.class).set(5, listeningPort1, rawStreaming()));
        serverTransport.handle(serverTransport.command(Listen.class).set(6, listeningPort2, rawStreaming()));
        assertThat(serverTransport.events().last(StartedListening.class, event -> event.commandId() == 5).port()).isEqualTo(listeningPort1);
        assertThat(serverTransport.events().last(StartedListening.class, event -> event.commandId() == 6).port()).isEqualTo(listeningPort2);

//        // When
        final int clientPort1 = freePortOtherThan(listeningPort1, listeningPort2);
        final int clientPort2 = freePortOtherThan(listeningPort1, listeningPort2, clientPort1);
        serverTransport.workUntil(completed(() -> clients.client(1).connectedTo(listeningPort1, clientPort1)));
        serverTransport.workUntil(completed(() -> clients.client(2).connectedTo(listeningPort2, clientPort2)));

        // Then
        serverTransport.workUntil(() -> serverTransport.events().all(ConnectionAccepted.class).size() >= 2);
        final ConnectionAccepted connectionAccepted1 = serverTransport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort1);
        final ConnectionAccepted connectionAccepted2 = serverTransport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort2);
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(2);

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
        serverTransport.handle(serverTransport.command(Listen.class).set(5, listeningPort1, rawStreaming()));
        assertThat(serverTransport.events().last(StartedListening.class, event -> event.commandId() == 5).port()).isEqualTo(listeningPort1);
        serverTransport.workUntil(completed(() -> clients.client(1).connectedTo(listeningPort1, clientPort1)));
        serverTransport.handle(serverTransport.command(Listen.class).set(6, listeningPort2, rawStreaming()));
        assertThat(serverTransport.events().last(StartedListening.class, event -> event.commandId() == 6).port()).isEqualTo(listeningPort2);
        serverTransport.workUntil(completed(() -> clients.client(2).connectedTo(listeningPort2, clientPort2)));

        // Then
        serverTransport.workUntil(() -> serverTransport.events().all(ConnectionAccepted.class).size() >= 2);
        final ConnectionAccepted connectionAccepted1 = serverTransport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort1);
        final ConnectionAccepted connectionAccepted2 = serverTransport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort2);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(2);

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
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(1);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);

        // When
        clients.client(1).close();
        serverTransport.workUntil(() -> serverTransport.events().contains(ConnectionClosed.class));

        // Then
        final ConnectionClosed connectionClosed = serverTransport.connectionEvents().last(ConnectionClosed.class, conn.connectionId());
        assertThat(connectionClosed).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(conn.port(), conn.connectionId(), CommandId.NO_COMMAND_ID));
        assertThat(serverTransport.events().contains(DataReceived.class)).isFalse();
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }

    @Test
    void shouldNotifyWhenRemoteEndpointEventuallyClosedConnection()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionId connection = driver.listenAndConnect(clients.client(1));
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(1);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(1);
        driver.successfullySendToClient(connection, clients.client(1), "foo");
        final DataSent lastDataSent = serverTransport.connectionEvents().last(DataSent.class, connection.connectionId());

        // When
        clients.client(1).close();
        serverTransport.workUntil(() -> serverTransport.events().contains(ConnectionClosed.class));

        // Then
        final ConnectionClosed connectionClosed = serverTransport.connectionEvents().last(ConnectionClosed.class, connection.connectionId());
        assertThat(connectionClosed).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(connection.port(), connection.connectionId(), CommandId.NO_COMMAND_ID));
        assertThat(serverTransport.events().contains(DataReceived.class)).isFalse();
        assertThat(serverTransport.connectionEvents().last(DataSent.class, connection.connectionId())).usingRecursiveComparison()
                .isEqualTo(lastDataSent);
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }

    @Test
    @Tag("tcperror")
    void shouldInformedThatConnectionResetByPeer()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        conn.port();
        conn.connectionId();
        serverTransport.handle(serverTransport.command(conn.connectionId(), SendData.class).set("foo".getBytes(US_ASCII)));
        conn.port();
        conn.connectionId();
        serverTransport.handle(serverTransport.command(conn.connectionId(), SendData.class).set("BA".getBytes(US_ASCII)));
        serverTransport.workUntil(() -> serverTransport.events().all(DataSent.class).size() == 2);

        //When
        clients.client(1).close();
        serverTransport.workUntil(() -> serverTransport.events().contains(ConnectionResetByPeer.class));
        serverTransport.workTimes(10);

        // Then
        assertEqual(
                serverTransport.events().all(ConnectionResetByPeer.class),
                new ConnectionResetByPeer(conn.port(), conn.connectionId(), CommandId.NO_COMMAND_ID)
        );
        assertThat(serverTransport.events().contains(ConnectionClosed.class)).isFalse();
        assertThat(serverTransport.events().contains(DataReceived.class)).isFalse();
        assertThat(serverTransport.statusEvents().all(NumberOfConnectionsChanged.class)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(serverTransport.statusEvents().last(NumberOfConnectionsChanged.class).newNumberOfConnections()).isEqualTo(0);
    }
}
