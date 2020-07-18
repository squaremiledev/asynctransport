package com.michaelszymczak.sample.sockets;

import java.net.SocketException;
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
import com.michaelszymczak.sample.sockets.support.FreePort;
import com.michaelszymczak.sample.sockets.support.SampleClients;
import com.michaelszymczak.sample.sockets.support.ThreadSafeReadDataSpy;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.TransportUnderTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.byteArrayWith;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.stringWith;
import static com.michaelszymczak.sample.sockets.support.TearDown.closeCleanly;
import static java.nio.charset.StandardCharsets.US_ASCII;

class DataSendingTest
{

    private final TransportUnderTest transport;
    private final SampleClients clients;

    DataSendingTest() throws SocketException
    {
        transport = new TransportUnderTest();
        clients = new SampleClients();
    }

    @SafeVarargs
    private static <T> Set<?> distinct(final Function<T, Object> property, final T... items)
    {
        final List<T> allItems = Arrays.asList(items);
        return allItems.stream().map(property).collect(Collectors.toSet());
    }

    @Test
    void shouldClaimAndSendData()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        final SendData command = transport.command(conn, SendData.class);
        final byte[] content = "foo".getBytes(US_ASCII);
        command.set(content);
        transport.handle(command);

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(3, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("foo");
        assertEqual(transport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), content.length, content.length));
    }

    @Test
    void shouldSendData()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        transport.handle(transport.command(conn, SendData.class).set("foo".getBytes(US_ASCII)));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(3, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("foo");
        assertEqual(transport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), "foo".getBytes(US_ASCII).length, "foo".getBytes(US_ASCII).length));
    }

    @Test
    void shouldSendDataManyTimes()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        transport.handle(transport.command(conn, SendData.class).set("foo".getBytes(US_ASCII)));
        transport.handle(transport.command(conn, SendData.class).set("BA".getBytes(US_ASCII)));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(5, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("fooBA");
        assertEqual(
                transport.events().all(DataSent.class),
                new DataSent(conn.port(), conn.connectionId(), "foo".getBytes(US_ASCII).length, "foo".getBytes(US_ASCII).length),
                new DataSent(conn.port(), conn.connectionId(), "BA".getBytes(US_ASCII).length, "fooBA".getBytes(US_ASCII).length)
        );
    }

    @Test
    void shouldNotifyThatAttemptToSendNoDataWasMade()
    {
        // Given
        final ConnectionAccepted conn = new TransportDriver(transport).listenAndConnect(clients.client(1));

        //When
        conn.port();
        conn.connectionId();
        transport.handle(transport.command(conn, SendData.class).set(new byte[]{}, (long)100));
        transport.workUntil(() -> transport.connectionEvents().contains(DataSent.class, conn.connectionId()));

        // Then
        assertEqual(transport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), 0, 0, 100));
    }

    @Test
    void shouldFailToSendDataUsingNonExistingConnectionOrPort()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        final int unusedPort = FreePort.freePortOtherThan(conn.port());

        //When
        transport.handle(new SendData(conn.port(), conn.connectionId() + 1, 20).set("foo".getBytes(US_ASCII), 108));
        transport.handle(new SendData(unusedPort, conn.connectionId(), 20).set("foo".getBytes(US_ASCII), 109));
        transport.handle(new SendData(conn.port(), conn.connectionId(), 20).set("bar".getBytes(US_ASCII), 110));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(3, 3, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");


        transport.workUntil(() -> transport.events().all(CommandFailed.class).size() > 1);
        assertThat(transport.events().lastResponse(CommandFailed.class, 108).port()).isEqualTo(conn.port());
        assertThat(transport.events().lastResponse(CommandFailed.class, 108).details()).containsIgnoringCase("connection id");
        assertThat(transport.events().lastResponse(CommandFailed.class, 109).port()).isEqualTo(unusedPort);
        assertThat(transport.events().lastResponse(CommandFailed.class, 109).details()).containsIgnoringCase("port");
    }

    @Test
    void shouldFailToSendDataUsingWrongPort()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        transport.handle(new SendData(conn.port() + 1, conn.connectionId(), 20).set("fo".getBytes(US_ASCII)));
        transport.handle(transport.command(conn, SendData.class).set("bar".getBytes(US_ASCII)));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(3, 3, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");

        transport.workUntil(() -> !transport.events().all(CommandFailed.class).isEmpty());
        assertThat(transport.events().last(CommandFailed.class).port()).isEqualTo(conn.port() + 1);
        assertThat(transport.events().last(CommandFailed.class).details()).containsIgnoringCase("port");
        assertThat(transport.events().last(DataSent.class)).usingRecursiveComparison()
                .isEqualTo(new DataSent(conn.port(), conn.connectionId(), "bar".getBytes(US_ASCII).length, "bar".getBytes(US_ASCII).length));
    }

    @Test
    void shouldSendDataViaMultipleConnections()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final StartedListening startedListeningEvent1 = driver.startListening();
        final ConnectionAccepted connS1C1 = driver.connectClient(startedListeningEvent1, clients.client(1));
        final StartedListening startedListeningEvent2 = driver.startListening();
        final ConnectionAccepted connS2C2 = driver.connectClient(startedListeningEvent2, clients.client(2));
        final ConnectionAccepted connS1C3 = driver.connectClient(startedListeningEvent1, clients.client(3));
        final ConnectionAccepted connS2C4 = driver.connectClient(startedListeningEvent2, clients.client(4));
        assertThat(distinct(ConnectionAccepted::commandId, connS1C1, connS2C2, connS1C3, connS2C4)).hasSize(2);
        assertThat(distinct(ConnectionAccepted::connectionId, connS1C1, connS2C2, connS1C3, connS2C4)).hasSize(4);


        //When
        connS1C1.port();
        connS1C1.connectionId();
        transport.handle(transport.command(connS1C1, SendData.class).set(fixedLengthStringStartingWith("S1 -> C1 ", 10).getBytes(US_ASCII)));
        connS2C2.port();
        connS2C2.connectionId();
        transport.handle(transport.command(connS2C2, SendData.class).set(fixedLengthStringStartingWith("S2 -> C2 ", 20).getBytes(US_ASCII)));
        connS1C3.port();
        connS1C3.connectionId();
        transport.handle(transport.command(connS1C3, SendData.class).set(fixedLengthStringStartingWith("S1 -> C3 ", 30).getBytes(US_ASCII)));
        connS2C4.port();
        connS2C4.connectionId();
        transport.handle(transport.command(connS2C4, SendData.class).set(fixedLengthStringStartingWith("S2 -> C4 ", 40).getBytes(US_ASCII)));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(10, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C1 ", 10));
        assertThat(transport.connectionEvents().last(DataSent.class, connS1C1.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C1.port(), connS1C1.connectionId(), 10, 10));

        transport.workUntil(completed(() -> clients.client(2).read(20, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C2 ", 20));
        assertThat(transport.connectionEvents().last(DataSent.class, connS2C2.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C2.port(), connS2C2.connectionId(), 20, 20));

        transport.workUntil(completed(() -> clients.client(3).read(30, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C3 ", 30));
        assertThat(transport.connectionEvents().last(DataSent.class, connS1C3.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C3.port(), connS1C3.connectionId(), 30, 30));

        transport.workUntil(completed(() -> clients.client(4).read(40, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C4 ", 40));
        assertThat(transport.connectionEvents().last(DataSent.class, connS2C4.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C4.port(), connS2C4.connectionId(), 40, 40));
    }

    @Test
    void shouldBeAbleToSendLargeChunkOfData()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final int contentSizeInBytes = 1_000_000;
        final TransportDriver driver = new TransportDriver(transport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);

        //When
        final byte[] data = byteArrayWith(pos -> String.format("%9d%n", pos), 100_000);
        assertThat(data.length).isEqualTo(contentSizeInBytes);
        conn.port();
        conn.connectionId();
        transport.handle(transport.command(conn, SendData.class).set(data));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(data.length, data.length, dataConsumer)));
        assertThat(dataConsumer.dataRead().length).isEqualTo(data.length);
        assertThat(stringWith(dataConsumer.dataRead())).isEqualTo(stringWith(data));
        final DataSent dataSentEvent = transport.connectionEvents().last(DataSent.class, conn.connectionId());
        assertThat(dataSentEvent).usingRecursiveComparison()
                .isEqualTo(new DataSent(conn.port(), conn.connectionId(), dataSentEvent.bytesSent(), data.length));

    }

    @Test
    void shouldBeAbleToSendDataInMultipleChunks()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);
        final byte[] dataThatFitsTheBuffer = generateData(conn.sendBufferSize(), 2);

        //When
        do
        {
            conn.port();
            conn.connectionId();
            transport.handle(transport.command(conn, SendData.class).set(dataThatFitsTheBuffer));
        }
        // TODO: waiting when invoking the 'last' method is probably not fit for purpose here
        while (transport.connectionEvents().last(DataSent.class, conn.connectionId()).bytesSent() != 0);

        // Then
        final long totalBytesSentUntilFilledTheSendQueue = transport.connectionEvents()
                .last(DataSent.class, conn.connectionId(), event -> event.bytesSent() == 0).totalBytesSent();
        assertThat(totalBytesSentUntilFilledTheSendQueue).isEqualTo((int)totalBytesSentUntilFilledTheSendQueue);
        assertThat(totalBytesSentUntilFilledTheSendQueue).isGreaterThanOrEqualTo(conn.sendBufferSize());
        transport.workUntil(completed(
                () -> clients.client(1).read((int)totalBytesSentUntilFilledTheSendQueue, (int)totalBytesSentUntilFilledTheSendQueue, dataConsumer)));

    }

    @Test
    void shouldBeAbleToSendPartOfTheData()
    {
        final TransportDriver driver = new TransportDriver(transport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);
        final int twiceThanSendBufferSize = Math.max(conn.sendBufferSize() * 2, 1_000_000);
        final byte[] data = byteArrayWith(pos -> String.format("%9d%n", pos), twiceThanSendBufferSize / 10);
        assertThat(data.length).isEqualTo(twiceThanSendBufferSize);


        //When
        transport.handle(transport.command(conn, SendData.class).set(data));
        transport.workUntil(() -> !transport.events().all(DataSent.class, event1 -> event1.connectionId() == conn.connectionId()).isEmpty());

        // Then
        final DataSent notAllDataSentEvent = transport.connectionEvents().last(DataSent.class, conn.connectionId());
        assertThat(notAllDataSentEvent.bytesSent()).isLessThan(data.length);
        // TODO: this can be tested when there is an internal buffer installed in the connection
        // assertThat(notAllDataSentEvent.totalBytesSent()).isEqualTo(data.length);
    }

    @AfterEach
    void tearDown()
    {
        closeCleanly(transport, clients, transport.statusEvents());
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

    private byte[] generateData(final int size, final int fraction)
    {
        final int sizeThatFitsSendBuffer = (size / (fraction * 10)) * 10;
        final byte[] data = byteArrayWith(pos -> String.format("%9d%n", pos), sizeThatFitsSendBuffer / 10);
        if (data.length != sizeThatFitsSendBuffer)
        {
            throw new RuntimeException("wrong size");
        }
        return data;
    }
}
