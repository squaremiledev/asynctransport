package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertSameSequence;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;

class ConnectingTransportTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();

    @Test
    void shouldNotifyWhenConnected() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);

        // Given
        transport.handle(new Listen(1, freePort()));
        final int serverPort = events.last(StartedListening.class).port();

        // When
        final int clientPort = freePort();
        runner.keepRunning(transport::doWork).untilCompleted(() -> new SampleClient().connectedTo(serverPort, clientPort));
        // TODO: hide this logic
        final long startTime = System.currentTimeMillis();
        while (events.all(ConnectionAccepted.class).size() < 1 && startTime + 100 > System.currentTimeMillis())
        {
            transport.doWork();
        }

        // Then
        assertSameSequence(events.all(ConnectionAccepted.class), new ConnectionAccepted(serverPort, 1, clientPort, 0));
    }

    @Test
    void shouldProvideConnectionDetailsForEachConnection() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);

        // Given
        transport.handle(new Listen(5, freePort()));
        final int serverPort = events.last(StartedListening.class).port();

        // When
        final BackgroundRunner.TaskToRun clientConnectsTask = () -> new SampleClient().connectedTo(serverPort);
        runner.keepRunning(transport::doWork).untilCompleted(clientConnectsTask);
        runner.keepRunning(transport::doWork).untilCompleted(clientConnectsTask);
        // TODO: hide this logic
        final long startTime = System.currentTimeMillis();
        while (events.all(ConnectionAccepted.class).size() < 2 && startTime + 100 > System.currentTimeMillis())
        {
            transport.doWork();
        }

        // Then
        final List<ConnectionAccepted> events = this.events.all(ConnectionAccepted.class);
        assertThat(events).hasSize(2);
        assertThat(events.get(0).commandId()).isEqualTo(events.get(1).commandId());
        assertThat(events.get(0).port()).isEqualTo(events.get(1).port());
        assertThat(events.get(0).remotePort()).isNotEqualTo(events.get(1).remotePort());
        assertThat(events.get(0).connectionId()).isNotEqualTo(events.get(1).connectionId());
    }

    @Test
        // TODO: this test fails eventually after multiple runs - maybe it's because of not releasing the port?
    void shouldCloseConnection() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);

        // Given
        transport.handle(new Listen(9, freePort()));
        long startTime1 = System.currentTimeMillis();
        // TODO: hide this logic
        while (events.all(StartedListening.class).size() < 1 && startTime1 + 100 > System.currentTimeMillis())
        {
            transport.doWork();
        }
        final int serverPort = events.last(StartedListening.class).port();
        final SampleClient client = new SampleClient();
        assertThrows(SocketException.class, client::write); // throws if not connected when writing
        runner.keepRunning(transport::doWork).untilCompleted(() -> client.connectedTo(serverPort));
        // TODO: hide this logic
        long startTime2 = System.currentTimeMillis();
        while (events.all(ConnectionAccepted.class).size() < 1 && startTime2 + 100 > System.currentTimeMillis())
        {
            transport.doWork();
        }
        final ConnectionAccepted connectionAccepted = events.last(ConnectionAccepted.class);

        // When
        transport.handle(new CloseConnection(connectionAccepted.port(), connectionAccepted.connectionId()));

        // Then
        assertThat(client.hasServerClosedConnection()).isTrue();
    }
}
