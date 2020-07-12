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


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static java.nio.charset.StandardCharsets.US_ASCII;

class DataTransferTest
{
    private final TransportEvents events = new TransportEvents();
    private final BackgroundRunner runner = new BackgroundRunner();
    private final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();

    @SafeVarargs
    private static <T> Set<?> distinct(final Function<T, Object> property, final T... items)
    {
        final List<T> allItems = Arrays.asList(items);
        return allItems.stream().map(property).collect(Collectors.toSet());
    }

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
        transport.handle(new SendData(conn.port(), conn.connectionId(), "BA".getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client.read(5, 10, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("fooBA");
        assertEqual(
                events.all(DataSent.class),
                new DataSent(conn.port(), conn.connectionId(), "foo".getBytes(US_ASCII).length),
                new DataSent(conn.port(), conn.connectionId(), "fooBA".getBytes(US_ASCII).length)
        );
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
        transport.handle(new SendData(conn.port() + 1, conn.connectionId(), "fo".getBytes(US_ASCII)));
        transport.handle(new SendData(conn.port(), conn.connectionId(), "bar".getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client.read(3, 3, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");

        workUntil(() -> !events.all(CommandFailed.class).isEmpty(), transport);
        assertThat(events.last(CommandFailed.class).port()).isEqualTo(conn.port() + 1);
        assertThat(events.last(CommandFailed.class).details()).containsIgnoringCase("port");
        assertThat(events.last(DataSent.class)).usingRecursiveComparison()
                .isEqualTo(new DataSent(conn.port(), conn.connectionId(), "bar".getBytes(US_ASCII).length));
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
        transport.handle(new SendData(connS1C1.port(), connS1C1.connectionId(), fixedLengthStringStartingWith("S1 -> C1 ", 10).getBytes(US_ASCII)));
        transport.handle(new SendData(connS2C2.port(), connS2C2.connectionId(), fixedLengthStringStartingWith("S2 -> C2 ", 20).getBytes(US_ASCII)));
        transport.handle(new SendData(connS1C3.port(), connS1C3.connectionId(), fixedLengthStringStartingWith("S1 -> C3 ", 30).getBytes(US_ASCII)));
        transport.handle(new SendData(connS2C4.port(), connS2C4.connectionId(), fixedLengthStringStartingWith("S2 -> C4 ", 40).getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client1.read(10, 100, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C1 ", 10));
        assertThat(events.last(DataSent.class, event -> event.connectionId() == connS1C1.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C1.port(), connS1C1.connectionId(), 10));

        runner.keepRunning(transport::work).untilCompleted(() -> client2.read(20, 100, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C2 ", 20));
        assertThat(events.last(DataSent.class, event -> event.connectionId() == connS2C2.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C2.port(), connS2C2.connectionId(), 20));

        runner.keepRunning(transport::work).untilCompleted(() -> client3.read(30, 100, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C3 ", 30));
        assertThat(events.last(DataSent.class, event -> event.connectionId() == connS1C3.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C3.port(), connS1C3.connectionId(), 30));

        runner.keepRunning(transport::work).untilCompleted(() -> client4.read(40, 100, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C4 ", 40));
        assertThat(events.last(DataSent.class, event -> event.connectionId() == connS2C4.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C4.port(), connS2C4.connectionId(), 40));
    }

    private String fixedLengthStringStartingWith(final String content, final int minLength)
    {
        final StringBuilder sb = new StringBuilder(10);
        sb.append(content);
        for (int i = 0; i < minLength - content.length(); i++)
        {
            sb.append(i % 10);
        }
        return sb.toString();
    }
}
