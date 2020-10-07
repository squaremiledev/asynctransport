package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.bufferWith;
import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.bytes;

class FixMessageDelineationTest
{
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
    private final FixMessageDelineation delineation = new FixMessageDelineation(delineatedDataSpy);

    private static byte[] ascii(final String content)
    {
        return content.replaceAll("^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }

    @Test
    void shouldIgnoreEmptyData()
    {
        delineation.onData(bufferWith(new byte[]{}), 0, 0);
        delineation.onData(bufferWith(new byte[]{1}), 1, 0);

        assertThat(delineatedDataSpy.received()).isEmpty();
    }

    @Test
    void shouldDetectFixMessage()
    {
        byte[] data = ascii("8=FIX.4.0^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^");
        delineation.onData(bufferWith(bytes(new byte[3], data, new byte[2])), 3, data.length);

        assertThat(delineatedDataSpy.received()).containsExactly(data);
    }
}