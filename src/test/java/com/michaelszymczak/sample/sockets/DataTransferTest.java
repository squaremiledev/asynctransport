package com.michaelszymczak.sample.sockets;

import java.io.IOException;

import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;

class DataTransferTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();

    @Test
    void shouldSendSomeData() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);

        // Given
        transport.handle(new Listen(1, freePort()));
        final int serverPort = events.last(StartedListening.class).port();
        final SampleClient client = new SampleClient();
        runner.keepRunning(transport::work).untilCompleted(() -> client.connectedTo(serverPort));
        workUntil(() -> !events.all(ConnectionAccepted.class).isEmpty(), transport);
        final ConnectionAccepted connectionAccepted = events.last(ConnectionAccepted.class);

        //When
        transport.handle(new SendData(connectionAccepted.port(), connectionAccepted.connectionId()));

        // Then
        // TODO: pass a synchronized data consumer to the client's read method
        runner.keepRunning(transport::work).untilCompleted(() -> client.read(1, 1));
    }
}
