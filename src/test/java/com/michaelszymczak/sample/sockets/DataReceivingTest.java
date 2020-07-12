package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.copyOf;

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
        workUntil(() -> !events.all(DataReceived.class, event -> event.totalBytesReceived() >= 3).isEmpty(), transport);

        // Then
        assertThat(events.all(DataReceived.class)).isNotEmpty();
        final DataReceived dataReceivedEvent = events.last(DataReceived.class);
        assertThat(dataReceivedEvent).usingRecursiveComparison()
                .isEqualTo(new DataReceived(conn.port(), conn.connectionId(), 3, dataReceivedEvent.data(), dataReceivedEvent.length()));
        assertThat(dataAsString(events.all(DataReceived.class), US_ASCII)).isEqualTo("foo");
    }

    private String dataAsString(final List<DataReceived> all, final Charset charset)
    {
        return all.stream()
                .map(dataReceived -> copyOf(dataReceived.data(), dataReceived.length()))
                .map(data -> new String(data, charset))
                .collect(Collectors.joining(""));
    }
}
