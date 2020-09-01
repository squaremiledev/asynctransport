package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.events.DataReceived;

import static java.nio.ByteBuffer.wrap;

class IntegerDataHandlerTest
{
    private MessageReceivedSpy messageReceivedSpy = new MessageReceivedSpy();
    private DataReceived dataReceived = new DataReceived(8888, 1, 0, 0, 100, wrap(new byte[100]));
    private IntegerDataHandler handler = new IntegerDataHandler(1, messageReceivedSpy);

    @Test
    void shouldNotNotifyOnNoData()
    {
        dataReceived.prepare();
        dataReceived.commit(0, 0);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).isEmpty();
    }

    @Test
    void shouldNotNotifyOnInsufficientData()
    {
        ByteBuffer buffer = dataReceived.prepare();
        buffer.put((byte)1).put((byte)2).put((byte)3);
        dataReceived.commit(3, 3);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).isEmpty();
    }

    @Test
    void shouldNotifyAboutReceivedInteger()
    {
        dataReceived.prepare().putInt(1295619689);
        dataReceived.commit(4, 4);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(1);
        assertThat(messageReceivedSpy.asPdus().get(0)).hasSize(4);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getInt()).isEqualTo(1295619689);
    }

    @Test
    void shouldNotifyAboutReceivedFullIntegersOnly()
    {
        ByteBuffer buffer = dataReceived.prepare();
        buffer.putInt(1295619689);
        buffer.put((byte)1).put((byte)2);
        dataReceived.commit(6, 6);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(1);
        assertThat(messageReceivedSpy.asPdus().get(0)).hasSize(4);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getInt()).isEqualTo(1295619689);
    }

    @Test
    void shouldHandleMultipleIntegersSent()
    {
        // When
        dataReceived.prepare().putInt(1);
        dataReceived.commit(4, 4);
        handler.onDataReceived(dataReceived);
        dataReceived.prepare().putInt(3);
        dataReceived.commit(4, 4);
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(2);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getInt()).isEqualTo(1);
        assertThat(wrap(messageReceivedSpy.asPdus().get(1)).getInt()).isEqualTo(3);
    }

    @Test
    void shouldHandleMultipleIntegersEncodedBackToBack()
    {
        dataReceived.prepare().putInt(1).putInt(2).putInt(3);
        dataReceived.commit(4 * 3, 4 * 3);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(3);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getInt()).isEqualTo(1);
        assertThat(wrap(messageReceivedSpy.asPdus().get(1)).getInt()).isEqualTo(2);
        assertThat(wrap(messageReceivedSpy.asPdus().get(2)).getInt()).isEqualTo(3);
    }

    @Test
    void shouldReassembleIntegers()
    {
        // When
        byte[] array = new byte[12];
        ByteBuffer source = wrap(array);
        source.putInt(100).putInt(200).putInt(300);

        // When
        ByteBuffer buffer1 = dataReceived.prepare();
        buffer1.put(array[0]);
        dataReceived.commit(1, 1);
        handler.onDataReceived(dataReceived);

        ByteBuffer buffer2 = dataReceived.prepare();
        buffer2.put(array[1]);
        buffer2.put(array[2]);
        buffer2.put(array[3]);
        buffer2.put(array[4]);
        buffer2.put(array[5]);
        buffer2.put(array[6]);
        dataReceived.commit(6, 6);
        handler.onDataReceived(dataReceived);

        ByteBuffer buffer3 = dataReceived.prepare();
        buffer3.put(array[7]);
        buffer3.put(array[8]);
        buffer3.put(array[9]);
        buffer3.put(array[10]);
        buffer3.put(array[11]);
        dataReceived.commit(5, 5);
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(3);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getInt()).isEqualTo(100);
        assertThat(wrap(messageReceivedSpy.asPdus().get(1)).getInt()).isEqualTo(200);
        assertThat(wrap(messageReceivedSpy.asPdus().get(2)).getInt()).isEqualTo(300);
    }
}