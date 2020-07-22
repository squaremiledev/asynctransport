package com.michaelszymczak.sample.sockets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.support.ConnectionEventsSpy;
import com.michaelszymczak.sample.sockets.support.TransportDriver;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static java.nio.charset.StandardCharsets.US_ASCII;


class DataReceivingTest extends TransportTestBase
{
    private static final int _10_MB_IN_BYTES = 10 * 1024 * 1024;

    @SafeVarargs
    private static <T> Set<?> distinct(final Function<T, Object> property, final T... items)
    {
        final List<T> allItems = Arrays.asList(items);
        return allItems.stream().map(property).collect(Collectors.toSet());
    }

    @Test
    void shouldReceiveData()
    {
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        // When
        transport.workUntil(completed(() -> clients.client(1).write("foo".getBytes(US_ASCII))));
        transport.workUntil(bytesReceived(transport.connectionEvents(), conn.connectionId(), 3));

        // Then
        assertThat(transport.events().all(DataReceived.class)).isNotEmpty();
        final DataReceived dataReceivedEvent = transport.events().last(DataReceived.class);
        assertThat(dataReceivedEvent.port()).isEqualTo(conn.port());
        assertThat(dataReceivedEvent.connectionId()).isEqualTo(conn.connectionId());
        assertThat(dataAsString(transport.events().all(DataReceived.class), US_ASCII)).isEqualTo("foo");
    }

    @Test
    void shouldEventuallyReceiveAllData()
    {
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        final List<byte[]> dataChunksToSend = Arrays.asList(
                bytes(fixedLengthStringStartingWith("\nfoo", conn.maxInboundMessageSize())),
                bytes(fixedLengthStringStartingWith("\nbar", conn.maxInboundMessageSize())),
                bytes(fixedLengthStringStartingWith("\nbazqux", conn.maxInboundMessageSize()))
        );
        byte[] wholeDataToSend = concatenatedData(dataChunksToSend);

        // When
        transport.workUntil(completed(() ->
                                      {
                                          for (byte[] dataChunk : dataChunksToSend)
                                          {
                                              clients.client(1).write(dataChunk);
                                          }
                                      }));
        transport.workUntil(bytesReceived(transport.connectionEvents(), conn.connectionId(), wholeDataToSend.length));

        // Then
        assertThat(transport.connectionEvents().all(DataReceived.class, conn.connectionId())).isNotEmpty();
        assertThat(transport.connectionEvents().last(DataReceived.class, conn.connectionId()).totalBytesReceived()).isEqualTo(wholeDataToSend.length);
        assertThat(transport.connectionEvents().all(DataReceived.class, conn.connectionId()).stream().mapToLong(DataReceived::length).sum()).isEqualTo(wholeDataToSend.length);
        assertThat(dataReceived(transport.connectionEvents().all(DataReceived.class, conn.connectionId()))).isEqualTo(wholeDataToSend);
    }

    @Test
    @Tag("tcperror")
    void shouldEventuallyReceiveAllTheDataSentAsOneLargeChunk()
    {
        final TransportDriver driver = new TransportDriver(transport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        final List<byte[]> dataChunksToSend = Arrays.asList(
                bytes(fixedLengthStringStartingWith("\nfoo", _10_MB_IN_BYTES / 2)),
                bytes(fixedLengthStringStartingWith("\nbar", _10_MB_IN_BYTES / 4)),
                bytes(fixedLengthStringStartingWith("\nbazqux", _10_MB_IN_BYTES / 2))
        );
        byte[] wholeDataToSend = concatenatedData(dataChunksToSend);
        assertThat(wholeDataToSend.length).isGreaterThan(_10_MB_IN_BYTES);

        // When
        transport.workUntil(completed(() -> clients.client(1).write(wholeDataToSend)));
        transport.workUntil(bytesReceived(transport.connectionEvents(), conn.connectionId(), wholeDataToSend.length));

        // Then
        assertThat(transport.connectionEvents().all(DataReceived.class, conn.connectionId())).isNotEmpty();
        assertThat(transport.connectionEvents().last(DataReceived.class, conn.connectionId()).totalBytesReceived()).isEqualTo(wholeDataToSend.length);
        assertThat(transport.connectionEvents().all(DataReceived.class, conn.connectionId()).stream().mapToLong(DataReceived::length).sum()).isEqualTo(wholeDataToSend.length);
        byte[] actualReceivedData = dataReceived(transport.connectionEvents().all(DataReceived.class, conn.connectionId()));
        assertThat(actualReceivedData).isEqualTo(wholeDataToSend);
        assertThat(actualReceivedData.length).isGreaterThan(_10_MB_IN_BYTES);
    }

    @Test
    void shouldReceivedDataFromMultipleConnections()
    {
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

        // When
        transport.workUntil(completed(() -> clients.client(1).write(fixedLengthStringStartingWith("S1 -> C1 ", 10).getBytes(US_ASCII))));
        transport.workUntil(completed(() -> clients.client(2).write(fixedLengthStringStartingWith("S2 -> C2 ", 20).getBytes(US_ASCII))));
        transport.workUntil(completed(() -> clients.client(3).write(fixedLengthStringStartingWith("S1 -> C3 ", 30).getBytes(US_ASCII))));
        transport.workUntil(completed(() -> clients.client(4).write(fixedLengthStringStartingWith("S2 -> C4 ", 40).getBytes(US_ASCII))));
        transport.workUntil(bytesReceived(transport.connectionEvents(), connS1C1.connectionId(), 10));
        transport.workUntil(bytesReceived(transport.connectionEvents(), connS2C2.connectionId(), 20));
        transport.workUntil(bytesReceived(transport.connectionEvents(), connS1C3.connectionId(), 30));
        transport.workUntil(bytesReceived(transport.connectionEvents(), connS2C4.connectionId(), 40));

        // Then
        assertThat(dataAsString(transport.connectionEvents().all(DataReceived.class, connS1C1.connectionId()), US_ASCII))
                .isEqualTo(fixedLengthStringStartingWith("S1 -> C1 ", 10));
        assertThat(dataAsString(transport.connectionEvents().all(DataReceived.class, connS2C2.connectionId()), US_ASCII))
                .isEqualTo(fixedLengthStringStartingWith("S2 -> C2 ", 20));
        assertThat(dataAsString(transport.connectionEvents().all(DataReceived.class, connS1C3.connectionId()), US_ASCII))
                .isEqualTo(fixedLengthStringStartingWith("S1 -> C3 ", 30));
        assertThat(dataAsString(transport.connectionEvents().all(DataReceived.class, connS2C4.connectionId()), US_ASCII))
                .isEqualTo(fixedLengthStringStartingWith("S2 -> C4 ", 40));
    }

    private byte[] dataReceived(final List<DataReceived> events)
    {
        return concatenatedData(events.stream().map(event ->
                                                    {
                                                        byte[] target = new byte[event.length()];
                                                        event.copyDataTo(target);
                                                        return target;
                                                    }).collect(Collectors.toList()));
    }

    private byte[] concatenatedData(final List<byte[]> allChunks)
    {
        int totalSize = allChunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] content = new byte[totalSize];
        ByteBuffer received = ByteBuffer.wrap(content);
        for (final byte[] chunk : allChunks)
        {
            received.put(chunk);
        }
        return content;
    }

    private byte[] bytes(final String foo)
    {
        return foo.getBytes(US_ASCII);
    }

    private BooleanSupplier bytesReceived(final ConnectionEventsSpy events, final long connectionId, final int size)
    {
        return () -> !events.all(DataReceived.class, connectionId, event -> event.totalBytesReceived() >= size).isEmpty();
    }

    private String dataAsString(final List<DataReceived> all, final Charset charset)
    {
        return all.stream()
                .map(dataReceived ->
                     {
                         byte[] target = new byte[dataReceived.length()];
                         dataReceived.copyDataTo(target);
                         return target;
                     })
                .map(data -> new String(data, charset))
                .collect(Collectors.joining(""));
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
