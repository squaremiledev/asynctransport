package dev.squaremile.tcpprobe;

import java.util.concurrent.ThreadLocalRandom;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataTest
{

    private final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
    private final DirectBuffer readBuffer = buffer;
    private final int offset = ThreadLocalRandom.current().nextInt(100);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldContainOptions(final boolean value)
    {
        new Metadata().wrap(buffer, offset).options().respond(value);
        assertThat(new Metadata().wrap(buffer, offset).options().respond()).isEqualTo(value);
        assertThat(new Metadata().wrap(readBuffer, offset).options().respond()).isEqualTo(value);
    }

    @Test
    void shouldClearOptions()
    {
        // Given
        assertThat(new Metadata().wrap(buffer, offset).options().respond()).isFalse();
        new Metadata().wrap(buffer, offset).options().respond(true);

        // When
        new Metadata().wrap(buffer, offset).clear();

        // Then
        assertThat(new Metadata().wrap(buffer, offset).options().respond()).isFalse();
        assertThat(new Metadata().wrap(readBuffer, offset).options().respond()).isFalse();
    }

    @Test
    void shouldContainMetadata()
    {
        Metadata metadata = new Metadata().wrap(buffer, offset);
        metadata.originalTimestampNs(1234L);
        metadata.correlationId(Long.MAX_VALUE);

        assertThat(new Metadata().wrap(buffer, offset).originalTimestampNs()).isEqualTo(1234L);
        assertThat(new Metadata().wrap(readBuffer, offset).originalTimestampNs()).isEqualTo(1234L);
        assertThat(new Metadata().wrap(buffer, offset).correlationId()).isEqualTo(Long.MAX_VALUE);
        assertThat(new Metadata().wrap(readBuffer, offset).correlationId()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void shouldDeclareTheAllocatedSize()
    {
        assertThat(new Metadata().length()).isEqualTo(4 + 8 + 8);
    }
}