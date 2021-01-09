package dev.squaremile.tcpprobe;

import java.util.concurrent.ThreadLocalRandom;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class OptionsTest
{
    private final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
    private final DirectBuffer readBuffer = buffer;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldDecideIfResponseRequired(final boolean value)
    {
        int offset = ThreadLocalRandom.current().nextInt(100);
        new Options().wrap(buffer, offset).respond(value);
        assertThat(new Options().wrap(buffer, offset).respond()).isEqualTo(value);
        assertThat(new Options().wrap(readBuffer, offset).respond()).isEqualTo(value);
    }
}