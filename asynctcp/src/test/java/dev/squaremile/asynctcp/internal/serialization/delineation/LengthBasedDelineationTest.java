package dev.squaremile.asynctcp.internal.serialization.delineation;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.values.Delineation;

import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.NOISE;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.PADDING;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.assertEquals;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.b;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.bufferWith;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.bytes;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.iValA;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.iValB;
import static dev.squaremile.asynctcp.api.transport.values.Delineation.Type.INT_BIG_ENDIAN_FIELD;

class LengthBasedDelineationTest
{
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();

    @Test
    void shouldNotNotifyAboutPartialData()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 0, 0, delineatedDataSpy);
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4}), 0, 3);
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 0, 0);
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 3, 0);

        assertThat(delineatedDataSpy.received()).isEmpty();
    }

    @Test
    void shouldReadTheLengthAndThenReadTheData()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 0, 0, delineatedDataSpy);
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
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 0, 0, delineatedDataSpy);
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
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 0, 0, delineatedDataSpy);
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
    void shouldTakeIntoAccountMessagePadding()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 3, 0, delineatedDataSpy);
        delineation.onData(bufferWith(bytes(
                new byte[3], // padding, 3 bytes
                intInBytes(2), // length, 4 bytes
                new byte[]{1, 2}, // data, 2 bytes
                new byte[]{99, 98, 0}, // new message, padding, 3 bytes
                intInBytes(4), // length, 4 bytes
                new byte[]{3, 4, 5, 6} // data, 4 bytes
        )), 0, 3 + 4 + 2 + 3 + 4 + 4);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2},
                new byte[]{3, 4, 5, 6}
        );
    }

    @Test
    void shouldUseBytesFromThePreviousBatch()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 1, 0, delineatedDataSpy);
        delineation.onData(bufferWith(bytes(b(NOISE), b(PADDING), intInBytes(4), iValB(), b(PADDING), intInBytes(4), b(iValA()[0], iValA()[1], NOISE))), 1, 4 + 4 + 4 + 2 + 2);
        delineation.onData(bufferWith(b(NOISE, iValA()[2], iValA()[3])), 1, 2);
        delineation.onData(bufferWith(bytes(b(PADDING), intInBytes(4), iValB())), 0, 9);

        assertEquals(
                delineatedDataSpy.received(),
                iValB(),
                iValA(),
                iValB()
        );
    }

    @Test
    void shouldUseLengthBytesFromThePreviousBatch()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 2, 0, delineatedDataSpy);
        delineation.onData(bufferWith(b(PADDING, PADDING, intInBytes(3)[0], intInBytes(3)[1], intInBytes(3)[2], intInBytes(3)[3])), 0, 6);
        delineation.onData(bufferWith(new byte[]{1, 2, 3}), 0, 3);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2, 3}
        );
    }

    @Test
    void shouldUseLengthBrokenDownIntoMultipleBatches()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 0, 0, delineatedDataSpy);
        delineation.onData(bufferWith(b(intInBytes(3)[0])), 0, 1);
        delineation.onData(bufferWith(b(intInBytes(3)[1], intInBytes(3)[2])), 0, 2);
        delineation.onData(bufferWith(b(intInBytes(3)[3])), 0, 1);
        delineation.onData(bufferWith(new byte[]{1, 2, 3}), 0, 3);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2, 3}
        );
    }

    @Test
    void shouldUseLengthBrokenDownIntoMultipleBatchesWithPadding()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 1, 0, delineatedDataSpy);
        delineation.onData(bufferWith(b(PADDING, intInBytes(4)[0])), 0, 2);
        delineation.onData(bufferWith(b(intInBytes(4)[1], intInBytes(4)[2])), 0, 2);
        delineation.onData(bufferWith(b(intInBytes(4)[3])), 0, 1);
        delineation.onData(bufferWith(new byte[]{1, 2, 3, 4}), 0, 4);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2, 3, 4}
        );
    }

    @Test
    void shouldHandlePaddingFillingTheWholeBatch()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 3, 0, delineatedDataSpy);
        delineation.onData(bufferWith(b(PADDING, PADDING, PADDING)), 0, 3);
        delineation.onData(bufferWith(intInBytes(2)), 0, 4);
        delineation.onData(bufferWith(new byte[]{1, 2}), 0, 2);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2}
        );
    }

    @Test
    void shouldHandlePaddingFillingMorThanOneBatch()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 4, 0, delineatedDataSpy);
        delineation.onData(bufferWith(b(NOISE, NOISE, PADDING, PADDING, PADDING)), 2, 3);
        delineation.onData(bufferWith(bytes(b(NOISE), b(PADDING), intInBytes(6))), 1, 5);
        delineation.onData(bufferWith(new byte[]{NOISE, NOISE, 1, 2, 3, NOISE}), 2, 3);
        delineation.onData(bufferWith(new byte[]{NOISE, NOISE, NOISE, NOISE, 4, 5, 6, NOISE}), 4, 4);

        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1, 2, 3, 4, 5, 6}
        );
    }

    @Test
    void shouldHandleZeroLengthMessage()
    {
        final DelineationHandler delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 0, 0, delineatedDataSpy);
        delineation.onData(bufferWith(new byte[3]), 0, 3);
        delineation.onData(bufferWith(new byte[6]), 0, 6);
        delineation.onData(bufferWith(new byte[3]), 0, 3);
        assertThat(delineatedDataSpy.received()).isEmpty();

        final DelineationHandler delineationNoPadding = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 0, 0, delineatedDataSpy);
        delineationNoPadding.onData(bufferWith(bytes(intInBytes(0), intInBytes(0))), 0, 8);
        delineationNoPadding.onData(bufferWith(bytes(intInBytes(0), intInBytes(0))), 0, 8);
        delineationNoPadding.onData(bufferWith(bytes(b(NOISE), intInBytes(0), intInBytes(0))), 1, 8);
        assertThat(delineatedDataSpy.received()).isEmpty();

        final DelineationHandler delineationWithPadding = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, 1, 0, delineatedDataSpy);
        delineationWithPadding.onData(bufferWith(bytes(b(PADDING), intInBytes(0), b(PADDING), intInBytes(0))), 0, 10);
        delineationWithPadding.onData(bufferWith(bytes(b(PADDING), intInBytes(0), b(PADDING), intInBytes(0))), 0, 10);
        delineationWithPadding.onData(bufferWith(bytes(b(NOISE), b(PADDING), intInBytes(0), b(PADDING), intInBytes(0))), 1, 10);
        assertThat(delineatedDataSpy.received()).isEmpty();
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