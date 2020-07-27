package com.michaelszymczak.sample.sockets.acceptancetests;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.michaelszymczak.sample.sockets.domain.api.CommandId;
import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.domain.api.events.DataSent;
import com.michaelszymczak.sample.sockets.domain.api.events.NumberOfConnectionsChanged;
import com.michaelszymczak.sample.sockets.domain.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportEvent;
import com.michaelszymczak.sample.sockets.support.FreePort;
import com.michaelszymczak.sample.sockets.support.ThreadSafeReadDataSpy;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.Worker;

import org.agrona.collections.MutableInteger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static com.michaelszymczak.sample.sockets.support.SampleClient.ReadDataConsumer.DEV_NULL;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.byteArrayWith;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.stringWith;
import static com.michaelszymczak.sample.sockets.support.Worker.runUntil;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class ServerSendsDataTest extends TransportTestBase
{

    @SafeVarargs
    private static <T> Set<?> distinct(final Function<T, Object> property, final T... items)
    {
        final List<T> allItems = asList(items);
        return allItems.stream().map(property).collect(Collectors.toSet());
    }

    @Test
    void shouldSendData()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        serverTransport.handle(serverTransport.command(conn, SendData.class).set(bytes("foo")));

        // Then
        serverTransport.workUntil(completed(() -> clients.client(1).read(3, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("foo");
        assertEqual(serverTransport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), 3, 3, 3));
    }

    @Test
    void shouldSendDataManyTimes()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        serverTransport.handle(serverTransport.command(conn, SendData.class).set(bytes("foo")));
        serverTransport.handle(serverTransport.command(conn, SendData.class).set(bytes("BA")));

        // Then
        serverTransport.workUntil(completed(() -> clients.client(1).read(5, 10, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("fooBA");
        assertEqual(
                serverTransport.events().all(DataSent.class),
                new DataSent(conn.port(), conn.connectionId(), 3, 3, 3),
                new DataSent(conn.port(), conn.connectionId(), 2, 5, 5)
        );
    }

    @Test
    void shouldNotifyThatAttemptToSendNoDataWasMade()
    {
        // Given
        final ConnectionAccepted conn = new TransportDriver(serverTransport).listenAndConnect(clients.client(1));

        //When
        conn.port();
        conn.connectionId();
        serverTransport.handle(serverTransport.command(conn, SendData.class).set(new byte[]{}, (long)100));
        serverTransport.workUntil(() -> serverTransport.connectionEvents().contains(DataSent.class, conn.connectionId()));

        // Then
        assertEqual(serverTransport.events().all(DataSent.class), new DataSent(conn.port(), conn.connectionId(), 0, 0, 0, 100));
    }

    @Test
    void shouldFailToSendDataUsingNonExistingConnectionOrPort()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        final int unusedPort = FreePort.freePortOtherThan(conn.port());

        //When
        serverTransport.handle(new SendData(conn.port(), conn.connectionId() + 1, 20).set(bytes("foo"), 108));
        serverTransport.handle(new SendData(unusedPort, conn.connectionId(), 20).set(bytes("foo"), 109));
        serverTransport.handle(new SendData(conn.port(), conn.connectionId(), 20).set(bytes("bar"), 110));

        // Then
        serverTransport.workUntil(completed(() -> clients.client(1).read(3, 3, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");


        serverTransport.workUntil(() -> serverTransport.events().all(CommandFailed.class).size() > 1);
        assertThat(serverTransport.events().lastResponse(CommandFailed.class, 108).port()).isEqualTo(conn.port());
        assertThat(serverTransport.events().lastResponse(CommandFailed.class, 108).details()).containsIgnoringCase("connection id");
        assertThat(serverTransport.events().lastResponse(CommandFailed.class, 109).port()).isEqualTo(unusedPort);
        assertThat(serverTransport.events().lastResponse(CommandFailed.class, 109).details()).containsIgnoringCase("port");
    }

    @Test
    void shouldFailToSendDataUsingWrongPort()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        //When
        serverTransport.handle(new SendData(conn.port() + 1, conn.connectionId(), 20).set(bytes("fo")));
        serverTransport.handle(serverTransport.command(conn, SendData.class).set(bytes("bar")));

        // Then
        serverTransport.workUntil(completed(() -> clients.client(1).read(3, 3, dataConsumer)));
        assertThat(new String(dataConsumer.dataRead(), US_ASCII)).isEqualTo("bar");

        serverTransport.workUntil(() -> !serverTransport.events().all(CommandFailed.class).isEmpty());
        assertThat(serverTransport.events().last(CommandFailed.class).port()).isEqualTo(conn.port() + 1);
        assertThat(serverTransport.events().last(CommandFailed.class).details()).containsIgnoringCase("port");
        assertThat(serverTransport.events().last(DataSent.class)).usingRecursiveComparison()
                .isEqualTo(new DataSent(conn.port(), conn.connectionId(), 3, 3, 3));
    }

    @Test
    void shouldSendDataViaMultipleConnections()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

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
        serverTransport.handle(serverTransport.command(connS1C1, SendData.class).set(bytes(fixedLengthStringStartingWith("S1 -> C1 ", 10))));
        connS2C2.port();
        connS2C2.connectionId();
        serverTransport.handle(serverTransport.command(connS2C2, SendData.class).set(bytes(fixedLengthStringStartingWith("S2 -> C2 ", 20))));
        connS1C3.port();
        connS1C3.connectionId();
        serverTransport.handle(serverTransport.command(connS1C3, SendData.class).set(bytes(fixedLengthStringStartingWith("S1 -> C3 ", 30))));
        connS2C4.port();
        connS2C4.connectionId();
        serverTransport.handle(serverTransport.command(connS2C4, SendData.class).set(bytes(fixedLengthStringStartingWith("S2 -> C4 ", 40))));

        // Then
        final ThreadSafeReadDataSpy dataConsumer1 = new ThreadSafeReadDataSpy();
        serverTransport.workUntil(completed(() -> clients.client(1).read(10, 100, dataConsumer1)));
        assertThat(new String(dataConsumer1.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C1 ", 10));
        assertThat(serverTransport.connectionEvents().last(DataSent.class, connS1C1.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C1.port(), connS1C1.connectionId(), 10, 10, 10));

        final ThreadSafeReadDataSpy dataConsumer2 = new ThreadSafeReadDataSpy();
        serverTransport.workUntil(completed(() -> clients.client(2).read(20, 100, dataConsumer2)));
        assertThat(new String(dataConsumer2.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C2 ", 20));
        assertThat(serverTransport.connectionEvents().last(DataSent.class, connS2C2.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C2.port(), connS2C2.connectionId(), 20, 20, 20));

        final ThreadSafeReadDataSpy dataConsumer3 = new ThreadSafeReadDataSpy();
        serverTransport.workUntil(completed(() -> clients.client(3).read(30, 100, dataConsumer3)));
        assertThat(new String(dataConsumer3.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S1 -> C3 ", 30));
        assertThat(serverTransport.connectionEvents().last(DataSent.class, connS1C3.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS1C3.port(), connS1C3.connectionId(), 30, 30, 30));

        final ThreadSafeReadDataSpy dataConsumer4 = new ThreadSafeReadDataSpy();
        serverTransport.workUntil(completed(() -> clients.client(4).read(40, 100, dataConsumer4)));
        assertThat(new String(dataConsumer4.dataRead(), US_ASCII)).isEqualTo(fixedLengthStringStartingWith("S2 -> C4 ", 40));
        assertThat(serverTransport.connectionEvents().last(DataSent.class, connS2C4.connectionId())).usingRecursiveComparison()
                .isEqualTo(new DataSent(connS2C4.port(), connS2C4.connectionId(), 40, 40, 40));
    }

    @Test
    @Tag("tcperror")
    void shouldBeAbleToSendLargeChunkOfData()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final int contentSizeInBytes = 1_000_000;
        final TransportDriver driver = new TransportDriver(serverTransport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);

        //When
        final byte[] data = byteArrayWith(pos -> String.format("%9d%n", pos), 100_000);
        assertThat(data.length).isEqualTo(contentSizeInBytes);
        conn.port();
        conn.connectionId();
        serverTransport.handle(serverTransport.command(conn, SendData.class).set(data));

        // Then
        serverTransport.workUntil(completed(() -> clients.client(1).read(data.length, data.length, dataConsumer)));
        assertThat(dataConsumer.dataRead().length).isEqualTo(data.length);
        assertThat(stringWith(dataConsumer.dataRead())).isEqualTo(stringWith(data));
        final DataSent dataSentEvent = serverTransport.connectionEvents().last(DataSent.class, conn.connectionId());
        assertThat(dataSentEvent).usingRecursiveComparison()
                .isEqualTo(new DataSent(conn.port(), conn.connectionId(), dataSentEvent.bytesSent(), data.length, data.length));

    }

    @Test
    @Tag("tcperror")
    void shouldBeAbleToSendDataInMultipleChunks()
    {
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        final TransportDriver driver = new TransportDriver(serverTransport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);
        final byte[] dataThatFitsTheBuffer = generateData(conn.outboundPduLimit(), 2);

        //When
        runUntil(() ->
                 {
                     serverTransport.handle(serverTransport.command(conn, SendData.class).set(dataThatFitsTheBuffer));
                     return serverTransport.connectionEvents().contains(DataSent.class, conn.connectionId()) &&
                            serverTransport.connectionEvents().last(DataSent.class, conn.connectionId()).bytesSent() == 0;
                 });

        // Then
        final long totalBytesSentUntilFilledTheSendQueue = serverTransport.connectionEvents()
                .last(DataSent.class, conn.connectionId(), event -> event.bytesSent() == 0).totalBytesSent();
        assertThat(totalBytesSentUntilFilledTheSendQueue).isEqualTo((int)totalBytesSentUntilFilledTheSendQueue);
        assertThat(totalBytesSentUntilFilledTheSendQueue).isGreaterThanOrEqualTo(conn.outboundPduLimit());
        serverTransport.workUntil(completed(
                () -> clients.client(1).read((int)totalBytesSentUntilFilledTheSendQueue, (int)totalBytesSentUntilFilledTheSendQueue, dataConsumer)));

    }

    @Test
    @Tag("tcperror")
    void shouldSendAsMuchDataAsPossibleAndBufferTheRest()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);
        final ThreadSafeReadDataSpy dataConsumerForTheClient = new ThreadSafeReadDataSpy();
        final ThreadSafeReadDataSpy dataConsumerForTheTest = new ThreadSafeReadDataSpy();
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);
        final int totalNumberOfEventsBefore = serverTransport.events().all(TransportEvent.class).size();
        final byte[] singleMessageData = byteArrayWith(pos -> String.format("%9d%n", pos), conn.outboundPduLimit() / 10);
        assertThat(singleMessageData.length).isEqualTo(conn.outboundPduLimit());

        //When
        MutableInteger commandsCount = new MutableInteger(0);
        serverTransport.workUntil(() ->
                                  {
                                      serverTransport.handle(serverTransport.command(conn, SendData.class).set(singleMessageData, commandsCount.incrementAndGet()));
                                      dataConsumerForTheTest.consume(singleMessageData, singleMessageData.length);
                                      // stop when unable to send more data
                                      return !serverTransport.connectionEvents().all(DataSent.class, conn.connectionId()).isEmpty() &&
                                             serverTransport.connectionEvents().last(DataSent.class, conn.connectionId()).bytesSent() == 0;
                                  });
        final int commandsSentCount = commandsCount.get();

        // Then
        assertThat(serverTransport.events().all(TransportEvent.class)).hasSize(totalNumberOfEventsBefore + commandsSentCount);
        final DataSent lastEvent = serverTransport.connectionEvents().last(DataSent.class, conn.connectionId());
        assertThat(lastEvent.bytesSent()).isEqualTo(0);
        assertThat(lastEvent.commandId()).isEqualTo(commandsSentCount);
        final int dataSizeInAllCommands = singleMessageData.length * commandsSentCount;
        assertThat(lastEvent.totalBytesBuffered()).isEqualTo(dataSizeInAllCommands);
        final int totalDataSentByIndividualChunks = (int)serverTransport.connectionEvents().all(DataSent.class, conn.connectionId()).stream().mapToLong(DataSent::bytesSent).sum();
        assertThat(lastEvent.totalBytesSent()).isEqualTo(totalDataSentByIndividualChunks);
        serverTransport.workUntil(completed(
                () -> clients.client(1).read(totalDataSentByIndividualChunks, totalDataSentByIndividualChunks, dataConsumerForTheClient)));
        assertThat(dataConsumerForTheClient.dataRead()).hasSize(totalDataSentByIndividualChunks);
        assertThat(dataConsumerForTheClient.dataRead()).isEqualTo(Arrays.copyOf(dataConsumerForTheTest.dataRead(), dataConsumerForTheClient.dataRead().length));
    }

    @Test
    @Tag("tcperror")
    void shouldSendRemainingBufferedDataWhenWindowUnstuck()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);
        final int serverPort = freePort();
        final int clientPort = freePortOtherThan(serverPort);
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1), serverPort, clientPort);
        final DataSent eventAfterWindowFilled = driver.fillTheSendingWindow(conn, conn.outboundPduLimit());
        assertThat(eventAfterWindowFilled.bytesSent()).isEqualTo(0);
        int totalBytesBuffered = (int)(eventAfterWindowFilled.totalBytesBuffered() - eventAfterWindowFilled.totalBytesSent());
        assertThat(totalBytesBuffered).isGreaterThan(0);
        runUntil(completed(() -> clients.client(1).read((int)eventAfterWindowFilled.totalBytesSent(), (int)eventAfterWindowFilled.totalBytesSent(), DEV_NULL)));
        DataSent eventAfterClientReadAllDataSentSoFar = serverTransport.connectionEvents().last(DataSent.class, conn.connectionId());
        assertThat(eventAfterClientReadAllDataSentSoFar.totalBytesBuffered()).isEqualTo(eventAfterWindowFilled.totalBytesBuffered());
        assertThat(eventAfterClientReadAllDataSentSoFar.totalBytesSent()).isEqualTo(eventAfterWindowFilled.totalBytesSent());

        // When
        serverTransport.workUntil(() ->
                                  {
                                      DataSent lastEvent = serverTransport.connectionEvents().last(DataSent.class, conn.connectionId());
                                      return lastEvent.totalBytesSent() == lastEvent.totalBytesBuffered();
                                  });
        serverTransport.workUntil(completed(() -> clients.client(1).read(totalBytesBuffered, totalBytesBuffered, DEV_NULL)));
        final DataSent lastEventAfterAllDataDelivered = serverTransport.connectionEvents().last(DataSent.class, conn.connectionId());
        assertThat(lastEventAfterAllDataDelivered.totalBytesBuffered()).isEqualTo(eventAfterClientReadAllDataSentSoFar.totalBytesSent() + totalBytesBuffered);
    }

    @Test
    void shouldNotifyAboutClosedConnectionAndDoNotSendAnythingWhenAskedToSendDataWhenClientIsAlreadyGone()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        assertThat(serverTransport.statusEvents().all()).isEmpty();
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        List<TransportEvent> eventsBeforeClosed = serverTransport.events().all();
        assertEqual(serverTransport.statusEvents().all(), singletonList(new NumberOfConnectionsChanged(1)));
        clients.client(1).close();
        Worker.runUntil(() -> clients.client(1).hasServerClosedConnection());

        //When
        serverTransport.handle(serverTransport.command(conn, SendData.class).set(bytes("foo"), 101));

        // Then
        assertEqual(serverTransport.events().all(), eventsBeforeClosed, asList(
                new ConnectionClosed(conn.port(), conn.connectionId(), CommandId.NO_COMMAND_ID),
                new TransportCommandFailed(conn.port(), 101, "Connection id not found", SendData.class)
        ));
        assertEqual(serverTransport.statusEvents().all(), asList(new NumberOfConnectionsChanged(1), new NumberOfConnectionsChanged(0)));
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
