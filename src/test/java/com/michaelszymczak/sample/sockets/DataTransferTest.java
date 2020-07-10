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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DataTransferTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();

    @Test
    @Disabled
    void shouldSendSomeData() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);

        // Given
        transport.handle(new Listen(1, 5544));
        final int serverPort = events.last(StartedListening.class).port();
        final SampleClient client = new SampleClient();
        runner.keepRunning(transport::doWork).untilCompleted(() -> client.connectedTo(serverPort));
        final ConnectionAccepted connectionAccepted = events.last(ConnectionAccepted.class);

        //When
        transport.handle(new SendData(connectionAccepted.port(), connectionAccepted.connectionId()));

        runner.keepRunning(transport::doWork).untilCompleted(client::write);

//        transport.handle();
        // Then
        // TODO: finish the test
    }
}
