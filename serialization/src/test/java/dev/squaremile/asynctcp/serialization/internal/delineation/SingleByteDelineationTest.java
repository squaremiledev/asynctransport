package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.util.List;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static java.util.Arrays.asList;

class SingleByteDelineationTest
{
    @Test
    void shouldDelineateByteByByte()
    {
        DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
        SingleByteDelineation delineation = new SingleByteDelineation(delineatedDataSpy);

        // When
        delineation.onData(new UnsafeBuffer(new byte[]{0, 1, 2, 3, 4}), 1, 3);

        // Then
        assertEquals(
                delineatedDataSpy.received(),
                new byte[]{1},
                new byte[]{2},
                new byte[]{3}
        );
    }

    private void assertEquals(final List<byte[]> actual, final byte[]... expected)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(asList(expected));
    }

}