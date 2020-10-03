package dev.squaremile.asynctcp.transport.internal.transportencoding;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;

class SingleByteDataHandlerTest
{
    private MessageReceivedSpy messageReceivedSpy = new MessageReceivedSpy();
    private DataReceived dataReceived = new DataReceived(8888, 1, 0, 0, 100, ByteBuffer.wrap(new byte[100]));

    @Test
    void shouldNotDoAnythingWhenNoData()
    {
        SingleByteDataHandler handler = new SingleByteDataHandler(new ConnectionIdValue(8888, 1), messageReceivedSpy);
        dataReceived.prepareForWriting();
        dataReceived.commitWriting(0, 0);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).isEmpty();
    }

    @Test
    void shouldNotifyAboutSingleByteReceived()
    {
        SingleByteDataHandler handler = new SingleByteDataHandler(new ConnectionIdValue(8888, 1), messageReceivedSpy);
        dataReceived.prepareForWriting().put((byte)'x').put((byte)'y').put((byte)'z');
        dataReceived.commitWriting(3, 3);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        List<MessageReceived> received = messageReceivedSpy.all();
        assertThat(received).hasSize(3);
        MessageReceived firstMessage = received.get(0);
        assertThat(firstMessage.connectionId()).isEqualTo(1);
        assertThat(firstMessage.port()).isEqualTo(8888);
        assertThat(firstMessage.length()).isEqualTo(1);
        assertThat(firstMessage.buffer().getByte(firstMessage.offset())).isEqualTo((byte)'x');
        MessageReceived lastMessage = received.get(2);
        assertThat(lastMessage.connectionId()).isEqualTo(1);
        assertThat(lastMessage.port()).isEqualTo(8888);
        assertThat(lastMessage.length()).isEqualTo(1);
        assertThat(lastMessage.buffer().getByte(lastMessage.offset())).isEqualTo((byte)'z');
    }

    @Test
    void shouldNotifyAllUpdates()
    {
        SingleByteDataHandler handler = new SingleByteDataHandler(new ConnectionIdValue(8888, 1), messageReceivedSpy);

        // When
        dataReceived.prepareForWriting().put((byte)'x');
        dataReceived.commitWriting(1, 1);
        handler.onDataReceived(dataReceived);
        dataReceived.prepareForWriting().put((byte)'y');
        dataReceived.commitWriting(1, 1);
        handler.onDataReceived(dataReceived);

        // Then
        List<MessageReceived> received = messageReceivedSpy.all();
        assertThat(received).hasSize(2);
        MessageReceived firstMessage = received.get(0);
        assertThat(firstMessage.connectionId()).isEqualTo(1);
        assertThat(firstMessage.port()).isEqualTo(8888);
        assertThat(firstMessage.length()).isEqualTo(1);
        assertThat(firstMessage.buffer().getByte(0)).isEqualTo((byte)'x');
        MessageReceived lastMessage = received.get(1);
        assertThat(lastMessage.connectionId()).isEqualTo(1);
        assertThat(lastMessage.port()).isEqualTo(8888);
        assertThat(lastMessage.length()).isEqualTo(1);
        assertThat(lastMessage.buffer().getByte(0)).isEqualTo((byte)'y');
        assertEqual(
                messageReceivedSpy.asPdus(),
                new byte[]{'x'},
                new byte[]{'y'}
        );
    }
}