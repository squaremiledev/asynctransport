package com.michaelszymczak.sample.sockets;

import java.io.IOException;

import com.michaelszymczak.sample.sockets.commands.Listen;
import com.michaelszymczak.sample.sockets.events.ConnectionEstablished;
import com.michaelszymczak.sample.sockets.events.StartedListening;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertSameSequence;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;

class ConnectingTransportTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();

    @Test
    void shouldNotifyWhenConnected() throws IOException
    {
        final Transport transport = new Transport(events);

        // Given
        transport.handle(new Listen(1, freePort()));
        final int port = events.last(StartedListening.class).port();

        // When
        runner.keepRunning(transport::doWork)
                .untilCompleted(() -> new SampleClient().connectedTo(port));

        // Then
        assertSameSequence(events.all(ConnectionEstablished.class), new ConnectionEstablished(port, 1));
    }

}
