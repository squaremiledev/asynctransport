package com.michaelszymczak.sample.sockets;

import java.io.IOException;

import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static java.nio.charset.StandardCharsets.US_ASCII;

class DataReceivingTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();

    @Test
    void shouldReceiveData() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport, events);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(client);

        // When
        runner.keepRunning(transport::work).untilCompleted(() -> client.write("foo".getBytes(US_ASCII)));

        // Then
        assertThat(events.all(DataReceived.class)).isNotEmpty();
        assertThat(events.last(DataReceived.class)).usingRecursiveComparison()
                .isEqualTo(new DataReceived(conn.port(), conn.connectionId(), "foo".getBytes(US_ASCII).length));
    }
}
