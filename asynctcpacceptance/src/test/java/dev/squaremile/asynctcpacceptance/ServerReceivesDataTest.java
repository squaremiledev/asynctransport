package dev.squaremile.asynctcpacceptance;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.testfitures.ConnectionEventsSpy;

import static dev.squaremile.asynctcp.testfitures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.testfitures.DataFixtures.concatenatedData;
import static dev.squaremile.asynctcp.testfitures.StringFixtures.byteArrayWith;
import static dev.squaremile.asynctcp.testfitures.StringFixtures.fixedLengthStringStartingWith;
import static dev.squaremile.asynctcp.testfitures.StringFixtures.stringWith;
import static java.nio.charset.StandardCharsets.US_ASCII;


class ServerReceivesDataTest extends TransportTestBase
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
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));

        // When
        serverTransport.workUntil(completed(() -> clients.client(1).write("foo".getBytes(US_ASCII))));
        serverTransport.workUntil(bytesReceived(serverTransport.connectionEvents(), conn.connectionId(), 3));

        // Then
        assertThat(serverTransport.events().all(DataReceived.class)).isNotEmpty();
        final DataReceived dataReceivedEvent = serverTransport.events().last(DataReceived.class);
        assertThat(dataReceivedEvent.port()).isEqualTo(conn.port());
        assertThat(dataReceivedEvent.connectionId()).isEqualTo(conn.connectionId());
        assertThat(stringWith(extractedContent(serverTransport.events().all(DataReceived.class)))).isEqualTo("foo");
    }

    @Test
    void shouldEventuallyReceiveAllData()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        final List<byte[]> dataChunksToSend = Arrays.asList(
                byteArrayWith(fixedLengthStringStartingWith("\nfoo", conn.inboundPduLimit())),
                byteArrayWith(fixedLengthStringStartingWith("\nbar", conn.inboundPduLimit())),
                byteArrayWith(fixedLengthStringStartingWith("\nbazqux", conn.inboundPduLimit()))
        );
        byte[] wholeDataToSend = concatenatedData(dataChunksToSend);

        // When
        serverTransport.workUntil(completed(() ->
                                            {
                                                for (byte[] dataChunk : dataChunksToSend)
                                                {
                                                    clients.client(1).write(dataChunk);
                                                }
                                            }));
        serverTransport.workUntil(bytesReceived(serverTransport.connectionEvents(), conn.connectionId(), wholeDataToSend.length));

        // Then
        assertThat(serverTransport.connectionEvents().all(DataReceived.class, conn.connectionId())).isNotEmpty();
        assertThat(serverTransport.connectionEvents().last(DataReceived.class, conn.connectionId()).totalBytesReceived()).isEqualTo(wholeDataToSend.length);
        assertThat(serverTransport.connectionEvents().all(DataReceived.class, conn.connectionId()).stream().mapToLong(DataReceived::length).sum()).isEqualTo(wholeDataToSend.length);
        assertThat(extractedContent(serverTransport.connectionEvents().all(DataReceived.class, conn.connectionId()))).isEqualTo(wholeDataToSend);
    }

    @Test
    @Tag("tcperror")
    void shouldEventuallyReceiveAllTheDataSentAsOneLargeChunk()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clients.client(1));
        final List<byte[]> dataChunksToSend = Arrays.asList(
                byteArrayWith(fixedLengthStringStartingWith("\nfoo", _10_MB_IN_BYTES / 2)),
                byteArrayWith(fixedLengthStringStartingWith("\nbar", _10_MB_IN_BYTES / 4)),
                byteArrayWith(fixedLengthStringStartingWith("\nbazqux", _10_MB_IN_BYTES / 2))
        );
        byte[] wholeDataToSend = concatenatedData(dataChunksToSend);
        assertThat(wholeDataToSend.length).isGreaterThan(_10_MB_IN_BYTES);

        // When
        serverTransport.workUntil(completed(() -> clients.client(1).write(wholeDataToSend)));
        serverTransport.workUntil(bytesReceived(serverTransport.connectionEvents(), conn.connectionId(), wholeDataToSend.length));

        // Then
        assertThat(serverTransport.connectionEvents().all(DataReceived.class, conn.connectionId())).isNotEmpty();
        assertThat(serverTransport.connectionEvents().last(DataReceived.class, conn.connectionId()).totalBytesReceived()).isEqualTo(wholeDataToSend.length);
        assertThat(serverTransport.connectionEvents().all(DataReceived.class, conn.connectionId()).stream().mapToLong(DataReceived::length).sum()).isEqualTo(wholeDataToSend.length);
        byte[] actualReceivedData = extractedContent(serverTransport.connectionEvents().all(DataReceived.class, conn.connectionId()));
        assertThat(actualReceivedData).isEqualTo(wholeDataToSend);
        assertThat(actualReceivedData.length).isGreaterThan(_10_MB_IN_BYTES);
    }

    @Test
    void shouldReceivedDataFromMultipleConnections()
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

        // When
        serverTransport.workUntil(completed(() -> clients.client(1).write(fixedLengthStringStartingWith("S1 -> C1 ", 10).getBytes(US_ASCII))));
        serverTransport.workUntil(completed(() -> clients.client(2).write(fixedLengthStringStartingWith("S2 -> C2 ", 20).getBytes(US_ASCII))));
        serverTransport.workUntil(completed(() -> clients.client(3).write(fixedLengthStringStartingWith("S1 -> C3 ", 30).getBytes(US_ASCII))));
        serverTransport.workUntil(completed(() -> clients.client(4).write(fixedLengthStringStartingWith("S2 -> C4 ", 40).getBytes(US_ASCII))));
        serverTransport.workUntil(bytesReceived(serverTransport.connectionEvents(), connS1C1.connectionId(), 10));
        serverTransport.workUntil(bytesReceived(serverTransport.connectionEvents(), connS2C2.connectionId(), 20));
        serverTransport.workUntil(bytesReceived(serverTransport.connectionEvents(), connS1C3.connectionId(), 30));
        serverTransport.workUntil(bytesReceived(serverTransport.connectionEvents(), connS2C4.connectionId(), 40));

        // Then
        assertThat(stringWith(extractedContent(serverTransport.connectionEvents().all(DataReceived.class, connS1C1.connectionId()))))
                .isEqualTo(fixedLengthStringStartingWith("S1 -> C1 ", 10));
        assertThat(stringWith(extractedContent(serverTransport.connectionEvents().all(DataReceived.class, connS2C2.connectionId()))))
                .isEqualTo(fixedLengthStringStartingWith("S2 -> C2 ", 20));
        assertThat(stringWith(extractedContent(serverTransport.connectionEvents().all(DataReceived.class, connS1C3.connectionId()))))
                .isEqualTo(fixedLengthStringStartingWith("S1 -> C3 ", 30));
        assertThat(stringWith(extractedContent(serverTransport.connectionEvents().all(DataReceived.class, connS2C4.connectionId()))))
                .isEqualTo(fixedLengthStringStartingWith("S2 -> C4 ", 40));
    }


    private byte[] extractedContent(final List<DataReceived> receivedEvents)
    {
        ByteBuffer actualContent = ByteBuffer.allocate((int)receivedEvents.get(receivedEvents.size() - 1).totalBytesReceived());
        receivedEvents.forEach(event -> event.copyDataTo(actualContent));
        return actualContent.array();
    }

    private BooleanSupplier bytesReceived(final ConnectionEventsSpy events, final long connectionId, final int size)
    {
        return () -> !events.all(DataReceived.class, connectionId, event -> event.totalBytesReceived() >= size).isEmpty();
    }

}
