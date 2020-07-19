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
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;
import com.michaelszymczak.sample.sockets.support.FreePort;
import com.michaelszymczak.sample.sockets.support.SampleClients;
import com.michaelszymczak.sample.sockets.support.ThreadSafeReadDataSpy;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.TransportUnderTest;

import org.agrona.collections.MutableInteger;
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
        final byte[] content = bytes("foo");
        command.set(content);
        transport.handle(command);

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(3, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("foo");
        assertEqual(transport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), content.length, content.length, content.length));
    }

    @Test
    void shouldSendData()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        transport.handle(transport.command(conn, SendData.class).set(bytes("foo")));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(3, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("foo");
        assertEqual(transport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), 3, 3,
                                                                         3
        ));
    }

    @Test
    void shouldSendDataManyTimes()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        transport.handle(transport.command(conn, SendData.class).set(bytes("foo")));
        transport.handle(transport.command(conn, SendData.class).set(bytes("BA")));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(5, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("fooBA");
        assertEqual(
                transport.events().all(DataSent.class),
                new DataSent(conn.port(), conn.connectionId(), 3, 3, 3),
                new DataSent(conn.port(), conn.connectionId(), 2, 5, 5)
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
        assertEqual(transport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), 0, 0, 0, 100));
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
        transport.handle(new SendData(conn.port(), conn.connectionId() + 1, 20).set(bytes("foo"), 108));
        transport.handle(new SendData(unusedPort, conn.connectionId(), 20).set(bytes("foo"), 109));
        transport.handle(new SendData(conn.port(), conn.connectionId(), 20).set(bytes("bar"), 110));

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
        transport.handle(new SendData(conn.port() + 1, conn.connectionId(), 20).set(bytes("fo")));
        transport.handle(transport.command(conn, SendData.class).set(bytes("bar")));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(3, 3, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");

        transport.workUntil(() -> !transport.events().all(CommandFailed.class).isEmpty());
        assertThat(transport.events().last(CommandFailed.class).port()).isEqualTo(conn.port() + 1);
        assertThat(transport.events().last(CommandFailed.class).details()).containsIgnoringCase("port");
        assertThat(transport.events().last(DataSent.class)).usingRecursiveComparison()
                .isEqualTo(new DataSent(conn.port(), conn.connectionId(), 3, 3, 3));
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
        transport.handle(transport.command(connS1C1, SendData.class).set(bytes(fixedLengthStringStartingWith("S1 -> C1 ", 10))));
        connS2C2.port();
        connS2C2.connectionId();
        transport.handle(transport.command(connS2C2, SendData.class).set(bytes(fixedLengthStringStartingWith("S2 -> C2 ", 20))));
        connS1C3.port();
        connS1C3.connectionId();
        transport.handle(transport.command(connS1C3, SendData.class).set(bytes(fixedLengthStringStartingWith("S1 -> C3 ", 30))));
        connS2C4.port();
        connS2C4.connectionId();
        transport.handle(transport.command(connS2C4, SendData.class).set(bytes(fixedLengthStringStartingWith("S2 -> C4 ", 40))));

        // Then
        transport.workUntil(completed(() -> clients.client(1).read(10, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C1 ", 10));
        assertThat(transport.connectionEvents().last(DataSent.class, connS1C1.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C1.port(), connS1C1.connectionId(), 10, 10, 10));

        transport.workUntil(completed(() -> clients.client(2).read(20, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C2 ", 20));
        assertThat(transport.connectionEvents().last(DataSent.class, connS2C2.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C2.port(), connS2C2.connectionId(), 20, 20, 20));

        transport.workUntil(completed(() -> clients.client(3).read(30, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C3 ", 30));
        assertThat(transport.connectionEvents().last(DataSent.class, connS1C3.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C3.port(), connS1C3.connectionId(), 30, 30, 30));

        transport.workUntil(completed(() -> clients.client(4).read(40, 100, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C4 ", 40));
        assertThat(transport.connectionEvents().last(DataSent.class, connS2C4.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C4.port(), connS2C4.connectionId(), 40, 40, 40));
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
                .isEqualTo(new DataSent(conn.port(), conn.connectionId(), dataSentEvent.bytesSent(), data.length, data.length));

    }

    @Test
    void shouldBeAbleToSendDataInMultipleChunks()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(transport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);
        final byte[] dataThatFitsTheBuffer = generateData(conn.maxMessageSize(), 2);

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
        assertThat(totalBytesSentUntilFilledTheSendQueue).isGreaterThanOrEqualTo(conn.maxMessageSize());
        transport.workUntil(completed(
                () -> clients.client(1).read((int)totalBytesSentUntilFilledTheSendQueue, (int)totalBytesSentUntilFilledTheSendQueue, dataConsumer)));

    }

    @Test
    void shouldSendAsMuchDataAsPossibleAndBufferTheRest()
    {
        final TransportDriver driver = new TransportDriver(transport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);
        final int totalNumberOfEventsBefore = transport.events().all(TransportEvent.class).size();
        final byte[] singleMessageData = byteArrayWith(pos -> String.format("%9d%n", pos), conn.maxMessageSize() / 10);
        assertThat(singleMessageData.length).isEqualTo(conn.maxMessageSize());

        //When
        MutableInteger commandsCount = new MutableInteger(0);
        transport.workUntil(() ->
                            {
                                transport.handle(transport.command(conn, SendData.class).set(singleMessageData, commandsCount.incrementAndGet()));
                                // stop when unable to send more data
                                return !transport.connectionEvents().all(DataSent.class, conn.connectionId()).isEmpty() &&
                                       transport.connectionEvents().last(DataSent.class, conn.connectionId()).bytesSent() == 0;
                            });
        final int commandsSentCount = commandsCount.get();
        transport.workUntil(() -> transport.connectionEvents().last(DataSent.class, conn.connectionId()).commandId() == commandsSentCount);

        // Then
        assertThat(transport.events().all(TransportEvent.class)).hasSize(totalNumberOfEventsBefore + commandsSentCount);
        final DataSent lastEvent = transport.connectionEvents().last(DataSent.class, conn.connectionId());
        assertThat(lastEvent.commandId()).isEqualTo(commandsSentCount);
        final int dataSizeInAllCommands = singleMessageData.length * commandsSentCount;
        assertThat(lastEvent.totalBytesBuffered()).isEqualTo(dataSizeInAllCommands);
        final long totalDataSentByIndividualChunks = transport.connectionEvents().all(DataSent.class, conn.connectionId()).stream().mapToLong(DataSent::bytesSent).sum();
        assertThat(lastEvent.totalBytesSent()).isEqualTo(totalDataSentByIndividualChunks);
    }

    @AfterEach
    void tearDown()
    {
        closeCleanly(transport, clients, transport.statusEvents());
    }

    private byte[] bytes(final String foo)
    {
        return foo.getBytes(US_ASCII);
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
