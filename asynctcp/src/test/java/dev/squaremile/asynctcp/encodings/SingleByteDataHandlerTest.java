package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;

class SingleByteDataHandlerTest
{
    private MessageReceivedSpy messageReceivedSpy = new MessageReceivedSpy();
    private DataReceived dataReceived = new DataReceived(8888, 1, 0, 0, 100, ByteBuffer.wrap(new byte[100]));

    @Test
    void shouldNotDoAnythingWhenNoData()
    {
        SingleByteDataHandler handler = new SingleByteDataHandler(1, messageReceivedSpy);
        dataReceived.prepare();
        dataReceived.commit(0, 0);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).isEmpty();
    }

    @Test
    void shouldNotifyAboutSingleByteReceived()
    {
        SingleByteDataHandler handler = new SingleByteDataHandler(1, messageReceivedSpy);
        dataReceived.prepare().put((byte)'x').put((byte)'y').put((byte)'z');
        dataReceived.commit(3, 3);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        List<MessageReceived> received = messageReceivedSpy.all();
        assertThat(received).hasSize(3);
        MessageReceived firstMessage = received.get(0);
        assertThat(firstMessage.connectionId()).isEqualTo(1);
        assertThat(firstMessage.port()).isEqualTo(8888);
        assertThat(firstMessage.length()).isEqualTo(1);
        assertThat(firstMessage.data().get(0)).isEqualTo((byte)'x');
        MessageReceived lastMessage = received.get(2);
        assertThat(lastMessage.connectionId()).isEqualTo(1);
        assertThat(lastMessage.port()).isEqualTo(8888);
        assertThat(lastMessage.length()).isEqualTo(1);
        assertThat(lastMessage.data().get(0)).isEqualTo((byte)'z');
    }

    @Test
    void shouldNotifyAllUpdates()
    {
        SingleByteDataHandler handler = new SingleByteDataHandler(1, messageReceivedSpy);

        // When
        dataReceived.prepare().put((byte)'x');
        dataReceived.commit(1, 1);
        handler.onDataReceived(dataReceived);
        dataReceived.prepare().put((byte)'y');
        dataReceived.commit(1, 1);
        handler.onDataReceived(dataReceived);

        // Then
        List<MessageReceived> received = messageReceivedSpy.all();
        assertThat(received).hasSize(2);
        MessageReceived firstMessage = received.get(0);
        assertThat(firstMessage.connectionId()).isEqualTo(1);
        assertThat(firstMessage.port()).isEqualTo(8888);
        assertThat(firstMessage.length()).isEqualTo(1);
        assertThat(firstMessage.data().get(0)).isEqualTo((byte)'x');
        MessageReceived lastMessage = received.get(1);
        assertThat(lastMessage.connectionId()).isEqualTo(1);
        assertThat(lastMessage.port()).isEqualTo(8888);
        assertThat(lastMessage.length()).isEqualTo(1);
        assertThat(lastMessage.data().get(0)).isEqualTo((byte)'y');
    }
}