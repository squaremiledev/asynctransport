package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.assertEquals;
import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.bufferWith;
import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.lValA;
import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.lValB;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

class LongsDelineationTest
{
    private static final byte NOISE = 54;
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
    private final LongsDelineation delineation = new LongsDelineation(delineatedDataSpy);

    @Test
    void shouldNotNotifyAboutPartialData()
    {
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 0, 7);
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 0, 0);
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 3, 0);

        assertThat(delineatedDataSpy.received()).isEmpty();
    }

    @Test
    void shouldDelineateDataAsEightByteLong()
    {
        delineation.onData(bufferWith(bytes(MAX_VALUE)), 0, 8);

        assertEquals(
                delineatedDataSpy.received(),
                bytes(MAX_VALUE)
        );
    }

    @Test
    void shouldDelineateMultipleCompleteChunks()
    {
        delineation.onData(bufferWith(DataFixtures.bytes(bytes(MAX_VALUE), bytes(0L), bytes(MIN_VALUE), DataFixtures.bytes(new byte[]{1, 2}))), 0, 8 * 3 + 2);

        assertEquals(
                delineatedDataSpy.received(),
                bytes(MAX_VALUE),
                bytes(0),
                bytes(MIN_VALUE)
        );
    }

    @Test
    void shouldUseOffset()
    {
        delineation.onData(bufferWith(DataFixtures.bytes(new byte[]{1, 2}, bytes(MAX_VALUE))), 2, 8);

        assertEquals(
                delineatedDataSpy.received(),
                bytes(MAX_VALUE)
        );
    }

    @Test
    void shouldUseBytesFromThePreviousBatch()
    {
        delineation.onData(bufferWith(DataFixtures.bytes(DataFixtures.b(NOISE), lValB(), DataFixtures.b(lValA()[0], lValA()[1], lValA()[2], lValA()[3], lValA()[4], NOISE))), 1, 8 + 5);
        delineation.onData(bufferWith(DataFixtures.b(NOISE, lValA()[5], lValA()[6], lValA()[7], lValA()[7])), 1, 3);
        delineation.onData(bufferWith(lValB()), 0, 8);

        assertEquals(
                delineatedDataSpy.received(),
                lValB(),
                lValA(),
                lValB()
        );
    }

    @Test
    void shouldUseBytesFromMultiplePreviousBatches()
    {
        delineation.onData(bufferWith(DataFixtures.b(NOISE, NOISE, NOISE, lValA()[0], NOISE, NOISE)), 3, 1);
        delineation.onData(bufferWith(DataFixtures.b(NOISE, NOISE, lValA()[1], lValA()[2], NOISE)), 2, 2);
        delineation.onData(bufferWith(DataFixtures.b(NOISE, lValA()[3], lValA()[4], lValA()[5])), 1, 3);
        delineation.onData(bufferWith(DataFixtures.b(NOISE, NOISE, lValA()[6], lValA()[7], NOISE)), 2, 2);

        assertEquals(
                delineatedDataSpy.received(),
                lValA()
        );
    }

    private byte[] bytes(final long value)
    {
        byte[] content = new byte[8];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        contentBuffer.putLong(value);
        return content;
    }
}