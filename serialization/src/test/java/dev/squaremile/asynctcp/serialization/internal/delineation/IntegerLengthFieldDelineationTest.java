package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.assertEquals;
import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.bufferWith;
import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.bytes;

class IntegerLengthFieldDelineationTest
{
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();

    @Test
    void shouldReadTheLengthAndThenReadTheData()
    {
        final IntegerLengthFieldDelineation delineation = new IntegerLengthFieldDelineation(delineatedDataSpy, 0);
        delineation.onData(bufferWith(bytes(
                intInBytes(9),
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}
        )), 0, Integer.BYTES + 9);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}
        );
    }

    @Test
    void shouldReadTheLengthAndDataInterchangeably()
    {
        final IntegerLengthFieldDelineation delineation = new IntegerLengthFieldDelineation(delineatedDataSpy, 0);
        delineation.onData(bufferWith(bytes(
                intInBytes(Integer.BYTES),
                intInBytes(12345),
                intInBytes(Long.BYTES),
                longInBytes(987654321),
                intInBytes(3),
                new byte[]{1, 2, 3}
        )), 0, Integer.BYTES * 3 + Integer.BYTES + Long.BYTES + 3);

        assertEquals(
                delineatedDataSpy.received(),
                intInBytes(12345),
                longInBytes(987654321),
                new byte[]{1, 2, 3}
        );
    }

    @Test
    void shouldTakeIntoAccountMessageOffsetInTheBuffer()
    {
        final IntegerLengthFieldDelineation delineation = new IntegerLengthFieldDelineation(delineatedDataSpy, 0);
        final int offset = 3;
        delineation.onData(bufferWith(bytes(
                new byte[offset],
                intInBytes(2),
                new byte[]{1, 2},
                intInBytes(4),
                new byte[]{3, 4, 5, 6}
        )), offset, Integer.BYTES * 2 + 2 + 4);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2},
                new byte[]{3, 4, 5, 6}
        );
    }

    @Test
    @Disabled
    void shouldTakeIntoAccountMessagePadding()
    {
        final IntegerLengthFieldDelineation delineation = new IntegerLengthFieldDelineation(delineatedDataSpy, 2);
        delineation.onData(bufferWith(bytes(
                new byte[2], // length field offset
                intInBytes(2), // length
                new byte[]{1, 2}, // data
                new byte[] {99, 98}, // new message, length field offset
                intInBytes(4), // length
                new byte[]{3, 4, 5, 6} // data
        )), 0, Integer.BYTES * 2 + 2 + 4);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2},
                new byte[]{3, 4, 5, 6}
        );
    }

    private byte[] intInBytes(final int value)
    {
        byte[] content = new byte[4];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        contentBuffer.putInt(value);
        return content;
    }

    private byte[] longInBytes(final int value)
    {
        byte[] content = new byte[8];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        contentBuffer.putLong(value);
        return content;
    }
}