package com.michaelszymczak.sample.sockets;

import java.io.IOException;

import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.BackgroundRunner;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.ThreadSafeReadDataSpy;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
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
        final TransportDriver driver = new TransportDriver(transport, events);

        // Given
        final ConnectionAccepted conn1 = driver.listenAndConnect(client1, 101, freePort());
        final ConnectionAccepted conn2 = driver.listenAndConnect(client2, 102, freePortOtherThan(conn1.port()));

        //When
        transport.handle(new SendData(conn1.port(), conn1.connectionId(), "foo".getBytes(US_ASCII)));
        transport.handle(new SendData(conn2.port(), conn2.connectionId(), "FOOBAR".getBytes(US_ASCII)));

        // Then
        runner.keepRunning(transport::work).untilCompleted(() -> client1.read(3, 10, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("foo");
        runner.keepRunning(transport::work).untilCompleted(() -> client2.read(6, 10, dataConsumer));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("FOOBAR");
    }
}
