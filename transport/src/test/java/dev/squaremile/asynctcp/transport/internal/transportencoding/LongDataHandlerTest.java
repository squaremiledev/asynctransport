package dev.squaremile.asynctcp.transport.internal.transportencoding;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

import static java.nio.ByteBuffer.wrap;

class LongDataHandlerTest
{
    private MessageReceivedSpy messageReceivedSpy = new MessageReceivedSpy();
    private DataReceived dataReceived = new DataReceived(8888, 1, 0, 0, 100, wrap(new byte[100]));
    private LongDataHandler handler = new LongDataHandler(new ConnectionIdValue(8888, 1), messageReceivedSpy);

    @Test
    void shouldNotNotifyOnNoData()
    {
        dataReceived.prepareForWriting();
        dataReceived.commitWriting(0, 0);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).isEmpty();
    }

    @Test
    void shouldNotNotifyOnInsufficientData()
    {
        ByteBuffer buffer = dataReceived.prepareForWriting();
        buffer.put((byte)1).put((byte)2).put((byte)3);
        dataReceived.commitWriting(3, 3);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).isEmpty();
    }

    @Test
    void shouldNotifyAboutReceivedLong()
    {
        dataReceived.prepareForWriting().putLong(Long.MAX_VALUE);
        dataReceived.commitWriting(8, 8);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(1);
        assertThat(messageReceivedSpy.asPdus().get(0)).hasSize(8);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getLong()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void shouldNotifyAboutReceivedFullLongsOnly()
    {
        ByteBuffer buffer = dataReceived.prepareForWriting();
        buffer.putLong(1295619689L);
        buffer.put((byte)1).put((byte)2);
        dataReceived.commitWriting(10, 10);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(1);
        assertThat(messageReceivedSpy.asPdus().get(0)).hasSize(8);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getLong()).isEqualTo(1295619689L);
    }

    @Test
    void shouldHandleMultipleLongsSent()
    {
        // When
        dataReceived.prepareForWriting().putLong(1);
        dataReceived.commitWriting(8, 8);
        handler.onDataReceived(dataReceived);
        dataReceived.prepareForWriting().putLong(3);
        dataReceived.commitWriting(8, 8);
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(2);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getLong()).isEqualTo(1);
        assertThat(wrap(messageReceivedSpy.asPdus().get(1)).getLong()).isEqualTo(3);
    }

    @Test
    void shouldHandleMultipleLongsEncodedBackToBack()
    {
        dataReceived.prepareForWriting().putLong(1).putLong(2).putLong(3);
        dataReceived.commitWriting(8 * 3, 8 * 3);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(3);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getLong()).isEqualTo(1);
        assertThat(wrap(messageReceivedSpy.asPdus().get(1)).getLong()).isEqualTo(2);
        assertThat(wrap(messageReceivedSpy.asPdus().get(2)).getLong()).isEqualTo(3);
    }

    @Test
    void shouldReassembleLongs()
    {
        // When
        final byte[] srcArray = new byte[24];
        wrap(srcArray).putLong(Long.MIN_VALUE).putLong(200).putLong(Long.MAX_VALUE);

        // When
        ByteBuffer buffer1 = dataReceived.prepareForWriting();
        buffer1.put(srcArray[0]);
        dataReceived.commitWriting(1, 1);
        handler.onDataReceived(dataReceived);

        ByteBuffer buffer2 = dataReceived.prepareForWriting();
        buffer2.put(srcArray[1]);
        buffer2.put(srcArray[2]);
        buffer2.put(srcArray[3]);
        buffer2.put(srcArray[4]);
        buffer2.put(srcArray[5]);
        buffer2.put(srcArray[6]);
        buffer2.put(srcArray[7]);
        buffer2.put(srcArray[8]);
        buffer2.put(srcArray[9]);
        buffer2.put(srcArray[10]);
        dataReceived.commitWriting(10, 10);
        handler.onDataReceived(dataReceived);

        ByteBuffer buffer3 = dataReceived.prepareForWriting();
        buffer3.put(srcArray[11]);
        buffer3.put(srcArray[12]);
        buffer3.put(srcArray[13]);
        buffer3.put(srcArray[14]);
        buffer3.put(srcArray[15]);
        buffer3.put(srcArray[16]);
        buffer3.put(srcArray[17]);
        buffer3.put(srcArray[18]);
        buffer3.put(srcArray[19]);
        buffer3.put(srcArray[20]);
        buffer3.put(srcArray[21]);
        buffer3.put(srcArray[22]);
        buffer3.put(srcArray[23]);
        dataReceived.commitWriting(13, 13);
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(3);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getLong()).isEqualTo(Long.MIN_VALUE);
        assertThat(wrap(messageReceivedSpy.asPdus().get(1)).getLong()).isEqualTo(200);
        assertThat(wrap(messageReceivedSpy.asPdus().get(2)).getLong()).isEqualTo(Long.MAX_VALUE);
    }
}