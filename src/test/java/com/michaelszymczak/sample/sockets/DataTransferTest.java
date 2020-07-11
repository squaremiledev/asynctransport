package com.michaelszymczak.sample.sockets;

import java.io.IOException;

import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.ThreadSafeReadDataSpy;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static java.nio.charset.StandardCharsets.US_ASCII;

class DataTransferTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();
    private final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();

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
        transport.handle(new SendData(
                connectionAccepted.port(),
                connectionAccepted.connectionId(),
                "foo".getBytes(US_ASCII)
        ));
        transport.handle(new SendData(
                connectionAccepted.port(),
                connectionAccepted.connectionId(),
                "BAR".getBytes(US_ASCII)
        ));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client.read(6, 6, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("fooBAR");
    }
}
