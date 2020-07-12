package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.ThreadSafeReadDataSpy;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static java.nio.charset.StandardCharsets.US_ASCII;

class DataTransferTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();
    private final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();

    @Test
    void shouldSendData() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport, events);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(client);

        //When
        transport.handle(new SendData(conn.port(), conn.connectionId(), "foo".getBytes(US_ASCII)));
        transport.handle(new SendData(conn.port(), conn.connectionId(), "BAR".getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client.read(6, 6, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("fooBAR");
        final List<DataSent> dataSentEvents = events.all(DataSent.class);
        assertThat(dataSentEvents).hasSize(2);
        assertThat(dataSentEvents.get(0).connectionId()).isEqualTo(conn.connectionId());
        assertThat(dataSentEvents.get(1).connectionId()).isEqualTo(conn.connectionId());
        assertThat(dataSentEvents.get(0).port()).isEqualTo(conn.port());
        assertThat(dataSentEvents.get(1).port()).isEqualTo(conn.port());
    }

    @Test
    void shouldFailToSendDataUsingNonExistingConnection() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport, events);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(client);

        //When
        transport.handle(new SendData(conn.port(), conn.connectionId() + 1, "foo".getBytes(US_ASCII)));
        transport.handle(new SendData(conn.port(), conn.connectionId(), "bar".getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client.read(3, 3, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");

        workUntil(() -> !events.all(CommandFailed.class).isEmpty(), transport);
        assertThat(events.last(CommandFailed.class).port()).isEqualTo(conn.port());
        assertThat(events.last(CommandFailed.class).details()).containsIgnoringCase("connection id");
    }

    @Test
    void shouldFailToSendDataUsingWrongPort() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);
        final SampleClient client = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport, events);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(client);

        //When
        transport.handle(new SendData(conn.port() + 1, conn.connectionId(), "foo".getBytes(US_ASCII)));
        transport.handle(new SendData(conn.port(), conn.connectionId(), "bar".getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client.read(3, 3, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");

        workUntil(() -> !events.all(CommandFailed.class).isEmpty(), transport);
        assertThat(events.last(CommandFailed.class).port()).isEqualTo(conn.port() + 1);
        assertThat(events.last(CommandFailed.class).details()).containsIgnoringCase("port");
    }

    @Test
    void shouldSendDataViaMultipleConnections() throws IOException
    {
        final NIOBackedTransport transport = new NIOBackedTransport(events);
        final SampleClient client1 = new SampleClient();
        final SampleClient client2 = new SampleClient();
        final SampleClient client3 = new SampleClient();
        final SampleClient client4 = new SampleClient();
        final TransportDriver driver = new TransportDriver(transport, events);

        // Given
        final StartedListening startedListeningEvent1 = driver.startListening();
        final ConnectionAccepted connS1C1 = driver.connectClient(startedListeningEvent1, client1);
        final StartedListening startedListeningEvent2 = driver.startListening();
        final ConnectionAccepted connS2C2 = driver.connectClient(startedListeningEvent2, client2);
        final ConnectionAccepted connS1C3 = driver.connectClient(startedListeningEvent1, client3);
        final ConnectionAccepted connS2C4 = driver.connectClient(startedListeningEvent2, client4);
        assertThat(distinct(ConnectionAccepted::commandId, connS1C1, connS2C2, connS1C3, connS2C4)).hasSize(2);
        assertThat(distinct(ConnectionAccepted::connectionId, connS1C1, connS2C2, connS1C3, connS2C4)).hasSize(4);


        //When
        transport.handle(new SendData(connS1C1.port(), connS1C1.connectionId(), "S1 -> C1".getBytes(US_ASCII)));
        transport.handle(new SendData(connS2C2.port(), connS2C2.connectionId(), "S2 -> C2".getBytes(US_ASCII)));
        transport.handle(new SendData(connS1C3.port(), connS1C3.connectionId(), "S1 -> C3".getBytes(US_ASCII)));
        transport.handle(new SendData(connS2C4.port(), connS2C4.connectionId(), "S2 -> C4".getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client1.read("S1 -> C1".length(), 20, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("S1 -> C1");
        runner.keepRunning(transport::work).untilCompleted(() -> client2.read("S2 -> C2".length(), 20, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("S2 -> C2");
        runner.keepRunning(transport::work).untilCompleted(() -> client3.read("S1 -> C3".length(), 20, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("S1 -> C3");
        runner.keepRunning(transport::work).untilCompleted(() -> client4.read("S2 -> C4".length(), 20, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("S2 -> C4");
    }

    @SafeVarargs
    private static <T> Set<?> distinct(final Function<T, Object> property, final T... items)
    {
        final List<T> allItems = Arrays.asList(items);
        return allItems.stream().map(property).collect(Collectors.toSet());
    }
}
