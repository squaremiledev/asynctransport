package dev.squaremile.tcpcheck.probe;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class Metadata
{
    public static final int OPTIONS_LENGTH = 4;
    public static final int SEND_TIME_LENGTH = 8;
    public static final int CORRELATION_ID_LENGTH = 8;
    public static final int ALL_METADATA_FIELDS_TOTAL_LENGTH = OPTIONS_LENGTH + SEND_TIME_LENGTH + CORRELATION_ID_LENGTH;
    public static final int DEFAULT_OPTIONS_OFFSET = 0;
    public static final int DEFAULT_SEND_TIME_OFFSET = DEFAULT_OPTIONS_OFFSET + OPTIONS_LENGTH;
    public static final int DEFAULT_CORRELATION_ID_OFFSET = DEFAULT_SEND_TIME_OFFSET + SEND_TIME_LENGTH;
    private final int optionsOffset;
    private final int sendTimeOffset;
    private final int correlationIdOffset;
    private final Options options = new Options();
    private MutableDirectBuffer buffer;
    private DirectBuffer readBuffer;
    private int offset = 0;

    Metadata()
    {
        this(DEFAULT_OPTIONS_OFFSET, DEFAULT_SEND_TIME_OFFSET, DEFAULT_CORRELATION_ID_OFFSET);
    }

    Metadata(final int optionsOffset, final int sendTimeOffset, final int correlationIdOffset)
    {
        this.optionsOffset = optionsOffset;
        this.sendTimeOffset = sendTimeOffset;
        this.correlationIdOffset = correlationIdOffset;
        final FieldArea optionsArea = new FieldArea("options", optionsOffset, OPTIONS_LENGTH);
        final FieldArea sendTimeArea = new FieldArea("sendTime", sendTimeOffset, SEND_TIME_LENGTH);
        final FieldArea correlationIdArea = new FieldArea("correlationId", correlationIdOffset, CORRELATION_ID_LENGTH);
        if (optionsArea.overlaps(sendTimeArea) || optionsArea.overlaps(correlationIdArea) || sendTimeArea.overlaps(correlationIdArea))
        {
            throw new IllegalArgumentException("Some fields overlap: " + optionsArea + correlationIdArea + sendTimeArea);
        }
    }

    public Metadata wrap(final MutableDirectBuffer buffer, final int offset, final int availableLength)
    {
        if (
                availableLength < optionsOffset + OPTIONS_LENGTH ||
                availableLength < sendTimeOffset + SEND_TIME_LENGTH ||
                availableLength < correlationIdOffset + CORRELATION_ID_LENGTH
        )
        {
            throw new IllegalArgumentException("Insufficient length to encode all fields: " + availableLength);
        }
        this.buffer = buffer;
        this.readBuffer = buffer;
        this.offset = offset;
        options.wrap(buffer, offset + optionsOffset);
        return this;
    }

    public Metadata wrap(final DirectBuffer buffer, final int offset)
    {
        this.buffer = null;
        this.readBuffer = buffer;
        this.offset = offset;
        options.wrap(buffer, offset + optionsOffset);
        return this;
    }

    public Metadata clear()
    {
        options.clear();
        return this;
    }

    public Options options()
    {
        return options;
    }

    public Metadata originalTimestampNs(final long value)
    {
        buffer.putLong(offset + sendTimeOffset, value);
        return this;
    }

    public long originalTimestampNs()
    {
        return readBuffer.getLong(offset + sendTimeOffset);
    }

    public Metadata correlationId(final long value)
    {
        buffer.putLong(offset + correlationIdOffset, value);
        return this;
    }

    public long correlationId()
    {
        return readBuffer.getLong(offset + correlationIdOffset);
    }

    private static class FieldArea
    {
        final int startPosition;
        final int endPosition;
        private final String name;


        private FieldArea(final String name, final int startPosition, final int length)
        {
            this.name = name;
            if (startPosition < 0)
            {
                throw new IllegalArgumentException("position cannot be negative, was: " + startPosition);
            }
            this.startPosition = startPosition;
            this.endPosition = startPosition + length - 1;
        }

        public boolean overlaps(final FieldArea otherArea)
        {
            return
                    (this.startPosition <= otherArea.startPosition && this.endPosition >= otherArea.startPosition) ||
                    (otherArea.startPosition <= this.startPosition && otherArea.endPosition >= this.startPosition);
        }

        @Override
        public String toString()
        {
            return "FieldArea{" +
                   "name='" + name + '\'' +
                   ", startPosition=" + startPosition +
                   ", endPosition=" + endPosition +
                   '}';
        }
    }
}
