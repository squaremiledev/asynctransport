package dev.squaremile.asynctcp.internal.serialization.delineation;

import java.nio.ByteBuffer;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.NOISE;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.assertEquals;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.b;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.bufferWith;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.bytes;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.iValA;
import static dev.squaremile.asynctcp.internal.serialization.delineation.DataFixtures.iValB;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

class FixedLengthDelineationTest
{
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
    private final DelineationHandler delineation = new FixedLengthDelineation(delineatedDataSpy, Integer.BYTES);

    @Test
    void shouldNotNotifyAboutPartialData()
    {
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4}), 0, 3);
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 0, 0);
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 3, 0);

        assertThat(delineatedDataSpy.received()).isEmpty();
    }

    @Test
    void shouldDelineateDataAsFourByteInteger()
    {
        delineation.onData(bufferWith(intInBytes(MAX_VALUE)), 0, 4);

        assertEquals(
                delineatedDataSpy.received(),
                intInBytes(MAX_VALUE)
        );
    }

    @Test
    void shouldDelineateMultipleCompleteChunks()
    {
        delineation.onData(bufferWith(bytes(intInBytes(MAX_VALUE), intInBytes(0), intInBytes(MIN_VALUE), bytes(new byte[]{1, 2}))), 0, 4 * 3 + 2);

        assertEquals(
                delineatedDataSpy.received(),
                intInBytes(MAX_VALUE),
                intInBytes(0),
                intInBytes(MIN_VALUE)
        );
    }

    @Test
    void shouldUseOffset()
    {
        delineation.onData(bufferWith(bytes(new byte[]{1, 2}, intInBytes(MAX_VALUE))), 2, 4);

        assertEquals(
                delineatedDataSpy.received(),
                intInBytes(MAX_VALUE)
        );
    }

    @Test
    void shouldUseBytesFromThePreviousBatch()
    {
        delineation.onData(bufferWith(bytes(b(NOISE), iValB(), b(iValA()[0], iValA()[1], NOISE))), 1, 4 + 2);
        delineation.onData(bufferWith(b(NOISE, iValA()[2], iValA()[3])), 1, 2);
        delineation.onData(bufferWith(iValB()), 0, 4);

        assertEquals(
                delineatedDataSpy.received(),
                iValB(),
                iValA(),
                iValB()
        );
    }

    @Test
    void shouldUseBytesFromMultiplePreviousBatches()
    {
        delineation.onData(bufferWith(b(NOISE, NOISE, NOISE, iValA()[0], NOISE, NOISE)), 3, 1);
        delineation.onData(bufferWith(b(NOISE, NOISE, iValA()[1], iValA()[2], NOISE)), 2, 2);
        delineation.onData(bufferWith(b(NOISE, iValA()[3], iValB()[0], iValB()[1])), 1, 3);
        delineation.onData(bufferWith(b(NOISE, NOISE, iValB()[2], iValB()[3], NOISE)), 2, 2);

        assertEquals(
                delineatedDataSpy.received(),
                iValA(),
                iValB()
        );
    }

    @Test
    void shouldDelineateByteByByte()
    {
        DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();

        // When
        new FixedLengthDelineation(delineatedDataSpy, 1)
                .onData(new UnsafeBuffer(new byte[]{0, 1, 2, 3, 4}), 1, 3);

        // Then
        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1},
                new byte[]{2},
                new byte[]{3}
        );
    }


    private byte[] intInBytes(final int value)
    {
        byte[] content = new byte[4];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        contentBuffer.putInt(value);
        return content;
    }
}