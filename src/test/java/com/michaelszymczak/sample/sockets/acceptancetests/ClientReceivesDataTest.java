package com.michaelszymczak.sample.sockets.acceptancetests;

import java.nio.ByteBuffer;
import java.util.List;

import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.domain.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.support.TransportDriver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.StringFixtures.byteArrayWith;
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

    private byte[] extractedContent(final List<DataReceived> receivedEvents)
    {
        ByteBuffer actualContent = ByteBuffer.allocate((int)receivedEvents.get(receivedEvents.size() - 1).totalBytesReceived());
        receivedEvents.forEach(event -> event.copyDataTo(actualContent));
        return actualContent.array();
    }

}
