package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;
import java.util.List;

import org.agrona.concurrent.UnsafeBuffer;

import static org.assertj.core.api.Assertions.assertThat;


import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

class DataFixtures
{
    static final byte NOISE = 78;

    static byte[] b(final byte... bytes)
    {
        return bytes(bytes);
    }

    static byte[] bytes(final byte[]... bytes)
    {
        int totalLength = stream(bytes).mapToInt(barr -> barr.length).sum();
        byte[] content = new byte[totalLength];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        stream(bytes).forEachOrdered(contentBuffer::put);
        return content;
    }

    static byte[] lValA()
    {
        return new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
    }

    static byte[] lValB()
    {
        return new byte[]{11, 12, 13, 14, 15, 16, 17, 18};
    }

    static byte[] iValA()
    {
        return new byte[]{1, 2, 3, 4};
    }

    static byte[] iValB()
    {
        return new byte[]{11, 12, 13, 14};
    }

    static void assertEquals(final List<byte[]> actual, final byte[]... expected)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(asList(expected));
    }

    static UnsafeBuffer bufferWith(final byte[] bytes)
    {
        return new UnsafeBuffer(bytes);
    }
}
