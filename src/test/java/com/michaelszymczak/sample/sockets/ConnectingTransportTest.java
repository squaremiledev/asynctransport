package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionResetByPeer;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.TransportUnderTest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static java.nio.charset.StandardCharsets.US_ASCII;

class ConnectingTransportTest
{
    @Test
    void shouldNotifyWhenConnected()
    {
        final TransportUnderTest transport = new TransportUnderTest();

        // Given
        transport.handle(new Listen(1, freePort()));
        final int serverPort = transport.events().last(StartedListening.class).port();

        // When
        final int clientPort = freePort();
        transport.workUntil(completed(() -> new SampleClient().connectedTo(serverPort, clientPort)));
        transport.workUntil(() -> transport.events().contains(ConnectionAccepted.class));

        // Then
        assertThat(transport.events().all(ConnectionAccepted.class)).hasSize(1);
        final ConnectionAccepted connectionAcceptedEvent = transport.events().last(ConnectionAccepted.class);
        assertThat(connectionAcceptedEvent).usingRecursiveComparison()
                .isEqualTo(new ConnectionAccepted(serverPort, 1, clientPort, 0, connectionAcceptedEvent.sendBufferSize()));
    }

    @Test
    void shouldProvideConnectionDetailsForEachConnection()
    {
        final TransportUnderTest transport = new TransportUnderTest();

        // Given
        transport.handle(new Listen(5, freePort()));
        final int serverPort = transport.events().last(StartedListening.class).port();

        // When
        final BackgroundRunner.ThrowingRunnable clientConnectsTask = () -> new SampleClient().connectedTo(serverPort);
        transport.workUntil(completed(clientConnectsTask));
        transport.workUntil(completed(clientConnectsTask));
        transport.workUntil(() -> transport.events().all(ConnectionAccepted.class).size() >= 2);

        // Then
        final List<ConnectionAccepted> events = transport.events().all(ConnectionAccepted.class);
        assertThat(events).hasSize(2);
        assertThat(events.get(0).commandId()).isEqualTo(events.get(1).commandId());
        assertThat(events.get(0).port()).isEqualTo(events.get(1).port());
        assertThat(events.get(0).remotePort()).isNotEqualTo(events.get(1).remotePort());
        assertThat(events.get(0).connectionId()).isNotEqualTo(events.get(1).connectionId());
    }

    @Test
    void shouldCloseConnection() throws IOException
    {
        final TransportUnderTest transport = new TransportUnderTest();

        // Given
        transport.handle(new Listen(9, freePort()));
        transport.workUntil(() -> !transport.events().all(StartedListening.class).isEmpty());
        final int serverPort = transport.events().last(StartedListening.class).port();
        final SampleClient client = new SampleClient();
        assertThrows(SocketException.class, client::write); // throws if not connected when writing
        transport.workUntil(completed(() -> client.connectedTo(serverPort)));
        transport.workUntil(() -> !transport.events().all(ConnectionAccepted.class).isEmpty());
        final ConnectionAccepted connectionAccepted = transport.events().last(ConnectionAccepted.class);

        // When
        transport.handle(new CloseConnection(connectionAccepted.port(), connectionAccepted.connectionId(), 10));

        // Then
        assertThat(client.hasServerClosedConnection()).isTrue();
        assertThat(transport.events().last(ConnectionClosed.class)).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(connectionAccepted.port(), connectionAccepted.connectionId(), 10));
        assertThat(transport.events().all(ConnectionClosed.class)).hasSize(1);
    }

    @Test
    void shouldCloseConnectionOnce() throws IOException
    {
        final TransportUnderTest transport = new TransportUnderTest();
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(client);
        transport.handle(new CloseConnection(conn.port(), conn.connectionId(), 15));
        assertThat(transport.events().last(ConnectionClosed.class)).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(conn.port(), conn.connectionId(), 15));
        assertThat(transport.events().all(ConnectionClosed.class)).hasSize(1);
        assertThat(transport.events().all(TransportCommandFailed.class)).isEmpty();
        assertThat(client.hasServerClosedConnection()).isTrue();

        // When
        transport.handle(new CloseConnection(conn.port(), conn.connectionId(), 16));

        // Then
        assertThat(transport.events().last(TransportCommandFailed.class).commandId()).isEqualTo(16);
        assertThat(transport.events().all(ConnectionClosed.class)).hasSize(1);
    }

    @Test
    void shouldRejectClosingNonExistingConnection()
    {
        final TransportUnderTest transport = new TransportUnderTest();

        // When
        transport.handle(new CloseConnection(1234, 11111, 15));

        // Then
        assertThat(transport.events().last(TransportCommandFailed.class, event -> event.commandId() == 15).details()).containsIgnoringCase("connection id");
        assertThat(transport.events().last(TransportCommandFailed.class, event -> event.commandId() == 15).port()).isEqualTo(1234);
    }

    @Test
    void shouldNotifyWhenConnectedWhileListeningOnMultiplePorts()
    {
        final TransportUnderTest transport = new TransportUnderTest();

        // Given
        final int listeningPort1 = freePort();
        final int listeningPort2 = freePortOtherThan(listeningPort1);
        transport.handle(new Listen(5, listeningPort1));
        transport.handle(new Listen(6, listeningPort2));
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 5).port()).isEqualTo(listeningPort1);
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 6).port()).isEqualTo(listeningPort2);

//        // When
        final int clientPort1 = freePortOtherThan(listeningPort1, listeningPort2);
        final int clientPort2 = freePortOtherThan(listeningPort1, listeningPort2, clientPort1);
        transport.workUntil(completed(() -> new SampleClient().connectedTo(listeningPort1, clientPort1)));
        transport.workUntil(completed(() -> new SampleClient().connectedTo(listeningPort2, clientPort2)));

        // Then
        transport.workUntil(() -> transport.events().all(ConnectionAccepted.class).size() >= 2);
        final ConnectionAccepted connectionAccepted1 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort1);
        final ConnectionAccepted connectionAccepted2 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort2);

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
        final TransportUnderTest transport = new TransportUnderTest();

        // Given
        final int listeningPort1 = freePort();
        final int listeningPort2 = freePortOtherThan(listeningPort1);
        final int clientPort1 = freePortOtherThan(listeningPort1, listeningPort2);
        final int clientPort2 = freePortOtherThan(listeningPort1, listeningPort2, clientPort1);

        // When
        transport.handle(new Listen(5, listeningPort1));
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 5).port()).isEqualTo(listeningPort1);
        transport.workUntil(completed(() -> new SampleClient().connectedTo(listeningPort1, clientPort1)));
        transport.handle(new Listen(6, listeningPort2));
        assertThat(transport.events().last(StartedListening.class, event -> event.commandId() == 6).port()).isEqualTo(listeningPort2);
        transport.workUntil(completed(() -> new SampleClient().connectedTo(listeningPort2, clientPort2)));

        // Then
        transport.workUntil(() -> transport.events().all(ConnectionAccepted.class).size() >= 2);
        final ConnectionAccepted connectionAccepted1 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort1);
        final ConnectionAccepted connectionAccepted2 = transport.events().last(ConnectionAccepted.class, event -> event.port() == listeningPort2);

        assertThat(connectionAccepted1.connectionId()).isNotEqualTo(connectionAccepted2.connectionId());
        assertThat(connectionAccepted1.commandId()).isEqualTo(5);
        assertThat(connectionAccepted2.commandId()).isEqualTo(6);
        assertThat(connectionAccepted1.port()).isEqualTo(listeningPort1);
        assertThat(connectionAccepted2.port()).isEqualTo(listeningPort2);
        assertThat(connectionAccepted1.remotePort()).isNotEqualTo(connectionAccepted2.remotePort());
    }

    @Test
    void shouldNotifyWhenRemoteEndpointImmediatelyClosedConnection() throws IOException
    {
        final TransportUnderTest transport = new TransportUnderTest();
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(client);

        // When
        client.close();
        transport.workUntil(() -> transport.events().contains(ConnectionClosed.class));

        // Then
        final ConnectionClosed connectionClosed = transport.connectionEvents().last(ConnectionClosed.class, conn.connectionId());
        assertThat(connectionClosed).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(conn.port(), conn.connectionId(), TransportCommand.CONVENTIONAL_IGNORED_COMMAND_ID));
        assertThat(transport.events().contains(DataReceived.class)).isFalse();
    }

    @Test
    void shouldNotifyWhenRemoteEndpointEventuallyClosedConnection() throws IOException
    {
        final TransportUnderTest transport = new TransportUnderTest();
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionId connection = driver.listenAndConnect(client);
        driver.successfullySendToClient(connection, client, "foo");
        final DataSent lastDataSent = transport.connectionEvents().last(DataSent.class, connection.connectionId());

        // When
        client.close();
        transport.workUntil(() -> transport.events().contains(ConnectionClosed.class));

        // Then
        final ConnectionClosed connectionClosed = transport.connectionEvents().last(ConnectionClosed.class, connection.connectionId());
        assertThat(connectionClosed).usingRecursiveComparison()
                .isEqualTo(new ConnectionClosed(connection.port(), connection.connectionId(), TransportCommand.CONVENTIONAL_IGNORED_COMMAND_ID));
        assertThat(transport.events().contains(DataReceived.class)).isFalse();
        assertThat(transport.connectionEvents().last(DataSent.class, connection.connectionId())).usingRecursiveComparison()
                .isEqualTo(lastDataSent);
    }

    @Test
    void shouldInformedThatConnectionResetByPeer() throws SocketException
    {
        final TransportUnderTest transport = new TransportUnderTest();
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(client);
        transport.handle(new SendData(conn.port(), conn.connectionId(), "foo".getBytes(US_ASCII)));
        transport.handle(new SendData(conn.port(), conn.connectionId(), "BA".getBytes(US_ASCII)));
        transport.workUntil(() -> transport.events().all(DataSent.class).size() == 2);

        //When
        client.close();
        transport.workUntil(() -> transport.events().contains(ConnectionResetByPeer.class));
        transport.workTimes(10);

        // Then
        assertEqual(
                transport.events().all(ConnectionResetByPeer.class),
                new ConnectionResetByPeer(conn.port(), conn.connectionId(), TransportCommand.CONVENTIONAL_IGNORED_COMMAND_ID)
        );
        assertThat(transport.events().contains(ConnectionClosed.class)).isFalse();
        assertThat(transport.events().contains(DataReceived.class)).isFalse();
    }
}
