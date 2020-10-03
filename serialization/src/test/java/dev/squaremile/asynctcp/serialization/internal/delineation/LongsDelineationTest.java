package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;
import java.util.List;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

class LongsDelineationTest
{
    private static final byte NOISE = 54;
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
    private final LongsDelineation delineation = new LongsDelineation(delineatedDataSpy);

    private static byte[] b(final byte... bytes)
    {
        return bytes(bytes);
    }

    private static byte[] bytes(final byte[]... bytes)
    {
        int totalLength = stream(bytes).mapToInt(barr -> barr.length).sum();
        byte[] content = new byte[totalLength];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        stream(bytes).forEachOrdered(contentBuffer::put);
        return content;
    }

    private static byte[] valueB()
    {
        return new byte[]{11, 12, 13, 14, 15, 16, 17, 18};
    }

    private static byte[] valueA()
    {
        return new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
    }

    @Test
    void shouldNotNotifyAboutPartialData()
    {
        delineation.onData(bufferWith(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}), 0, 7);

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
        delineation.onData(bufferWith(bytes(bytes(MAX_VALUE), bytes(0L), bytes(MIN_VALUE), bytes(new byte[]{1, 2}))), 0, 8 * 3 + 2);

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
        delineation.onData(bufferWith(bytes(new byte[]{1, 2}, bytes(MAX_VALUE))), 2, 8);

        assertEquals(
                delineatedDataSpy.received(),
                bytes(MAX_VALUE)
        );
    }

    @Test
    void shouldUseBytesFromThePreviousBatch()
    {
        delineation.onData(bufferWith(bytes(b(NOISE), valueB(), b(valueA()[0], valueA()[1], valueA()[2], valueA()[3], valueA()[4], NOISE))), 1, 8 + 5);
        delineation.onData(bufferWith(b(NOISE, valueA()[5], valueA()[6], valueA()[7], valueA()[7])), 1, 3);
        delineation.onData(bufferWith(valueB()), 0, 8);

        assertEquals(
                delineatedDataSpy.received(),
                valueB(),
                valueA(),
                valueB()
        );
    }

    @Test
    void shouldUseBytesFromMultiplePreviousBatches()
    {
        delineation.onData(bufferWith(b(NOISE, NOISE, NOISE, valueA()[0], NOISE, NOISE)), 3, 1);
        delineation.onData(bufferWith(b(NOISE, NOISE, valueA()[1], valueA()[2], NOISE)), 2, 2);
        delineation.onData(bufferWith(b(NOISE, valueA()[3], valueA()[4], valueA()[5])), 1, 3);
        delineation.onData(bufferWith(b(NOISE, NOISE, valueA()[6], valueA()[7], NOISE)), 2, 2);

        assertEquals(
                delineatedDataSpy.received(),
                valueA()
        );
    }

    private UnsafeBuffer bufferWith(final byte[] bytes)
    {
        return new UnsafeBuffer(bytes);
    }

    private void assertEquals(final List<byte[]> actual, final byte[]... expected)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(asList(expected));
    }

    private byte[] bytes(final long value)
    {
        byte[] content = new byte[8];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        contentBuffer.putLong(value);
        return content;
    }
}