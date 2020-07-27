package com.michaelszymczak.sample.sockets.acceptancetests;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.events.Connected;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.domain.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.support.ConnectionEventsSpy;
import com.michaelszymczak.sample.sockets.support.TransportDriver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.DataFixtures.concatenatedData;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.byteArrayWith;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.fixedLengthStringStartingWith;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.stringWith;


class ClientReceivesDataTest extends TransportTestBase
{
    @Test
    void shouldReceiveData()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clientTransport);

        // When
        serverTransport.handle(new SendData(conn.port(), conn.connectionId(), 3).set(byteArrayWith("foo"), 101));
        spinUntil(() -> !clientTransport.connectionEvents().all(DataReceived.class).isEmpty());

        // Then
        assertThat(stringWith(extractedContent(clientTransport.connectionEvents().all(DataReceived.class)))).isEqualTo("foo");
    }

    @Test
    void shouldEventuallyReceiveAllData()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clientTransport);
        final Connected connected = clientTransport.connectionEvents().last(Connected.class);
        final List<byte[]> dataChunksToSend = Arrays.asList(
                byteArrayWith(fixedLengthStringStartingWith("\nfoo", conn.maxInboundMessageSize())),
                byteArrayWith(fixedLengthStringStartingWith("\nbar", conn.maxInboundMessageSize())),
                byteArrayWith(fixedLengthStringStartingWith("\nbazqux", conn.maxInboundMessageSize()))
        );
        byte[] wholeDataToSend = concatenatedData(dataChunksToSend);

        // When
        dataChunksToSend.forEach(dataChunkToSend -> serverTransport.handle(new SendData(conn, dataChunkToSend.length).set(dataChunkToSend)));
        spinUntil(bytesReceived(clientTransport.connectionEvents(), connected.connectionId(), wholeDataToSend.length));

        // Then
        assertThat(clientTransport.connectionEvents().all(DataReceived.class, connected.connectionId())).isNotEmpty();
        assertThat(clientTransport.connectionEvents().last(DataReceived.class, connected.connectionId()).totalBytesReceived()).isEqualTo(wholeDataToSend.length);
        assertThat(clientTransport.connectionEvents().all(DataReceived.class, connected.connectionId()).stream().mapToLong(DataReceived::length).sum()).isEqualTo(wholeDataToSend.length);
        assertThat(extractedContent(clientTransport.connectionEvents().all(DataReceived.class, connected.connectionId()))).isEqualTo(wholeDataToSend);
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
