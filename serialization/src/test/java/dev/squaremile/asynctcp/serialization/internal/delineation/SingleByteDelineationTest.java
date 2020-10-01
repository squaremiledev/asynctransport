package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static java.nio.ByteBuffer.wrap;
import static java.util.Arrays.asList;

class SingleByteDelineationTest
{
    @Test
    void shouldDelineateByteByByte()
    {
        DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
        SingleByteDelineation delineation = new SingleByteDelineation(delineatedDataSpy);

        // When
        delineation.onData(wrap(new byte[]{0, 1, 2, 3, 4}), 1, 3);

        // Then
        assertEquals(
                delineatedDataSpy.received(),
                new DelineatedDataSpy.Data(wrap(new byte[]{1}), 0, 1),
                new DelineatedDataSpy.Data(wrap(new byte[]{2}), 0, 1),
                new DelineatedDataSpy.Data(wrap(new byte[]{3}), 0, 1)
        );
    }

    private void assertEquals(final List<DelineatedDataSpy.Data> actual, final DelineatedDataSpy.Data... expected)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(asList(expected));
    }

    private static class DelineatedDataSpy implements DelineationHandler
    {
        private final List<Data> received = new ArrayList<>();

        @Override
        public void onData(final ByteBuffer byteBuffer, final int offset, final int length)
        {
            received.add(new Data(wrap(Arrays.copyOf(byteBuffer.array(), byteBuffer.array().length)), offset, length));
        }

        public List<Data> received()
        {
            return received;
        }

        static class Data
        {
            final ByteBuffer byteBuffer;
            final int offset;
            final int length;

            public Data(final ByteBuffer byteBuffer, final int offset, final int length)
            {
                this.byteBuffer = byteBuffer;
                this.offset = offset;
                this.length = length;
            }
        }
    }
}