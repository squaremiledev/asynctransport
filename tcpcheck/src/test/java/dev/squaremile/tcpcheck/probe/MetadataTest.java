package dev.squaremile.tcpcheck.probe;

import java.util.concurrent.ThreadLocalRandom;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataTest
{

    private final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
    private final DirectBuffer readBuffer = buffer;
    private final int offset = ThreadLocalRandom.current().nextInt(100);

    @Test
    void shouldRejectBufferThatDoesNotHaveEnoughSpaceToWrite()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH - 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Metadata(0, 10, 20).wrap(buffer, offset, 27)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Metadata(0, 20, 10).wrap(buffer, offset, 27)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Metadata(20, 0, 10).wrap(buffer, offset, 23)
        );
        // this is sufficient
        new Metadata(0, 10, 20).wrap(buffer, offset, 28);
    }

    @ParameterizedTest
    @CsvSource({
            "-1,4,12",
            "0,2,12",
            "0,3,12",
            "0,4,10",
            "0,4,11",
            "6,0,12",
            "7,0,12",
            "8,0,10",
            "8,0,11",
            "16,0,6",
            "16,0,7",
            "14,0,8",
            "15,0,8",
            "6,12,0",
            "7,12,0",
            "8,11,0",
            "8,10,0",
    })
    void shouldRejectMetadataConfigurationThatOverwritesItsOwnData(final int optionsOffset, final int sendTimeOffset, final int correlationIdOffset)
    {
        assertThrows(IllegalArgumentException.class, () -> new Metadata(optionsOffset, sendTimeOffset, correlationIdOffset));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldContainOptions(final boolean value)
    {
        new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).options().respond(value);
        assertThat(new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).options().respond()).isEqualTo(value);
        assertThat(new Metadata().wrap(readBuffer, offset).options().respond()).isEqualTo(value);
    }

    @Test
    void shouldClearOptions()
    {
        // Given
        assertThat(new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).options().respond()).isFalse();
        new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).options().respond(true);

        // When
        new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).clear();

        // Then
        assertThat(new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).options().respond()).isFalse();
        assertThat(new Metadata().wrap(readBuffer, offset).options().respond()).isFalse();
    }

    @Test
    void shouldContainMetadata()
    {
        Metadata metadata = new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH);
        metadata.originalTimestampNs(1234L);
        metadata.correlationId(Long.MAX_VALUE);

        assertThat(new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).originalTimestampNs()).isEqualTo(1234L);
        assertThat(new Metadata().wrap(readBuffer, offset).originalTimestampNs()).isEqualTo(1234L);
        assertThat(new Metadata().wrap(buffer, offset, Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH).correlationId()).isEqualTo(Long.MAX_VALUE);
        assertThat(new Metadata().wrap(readBuffer, offset).correlationId()).isEqualTo(Long.MAX_VALUE);
    }
}