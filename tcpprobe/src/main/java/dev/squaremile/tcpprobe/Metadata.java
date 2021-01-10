package dev.squaremile.tcpprobe;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

class Metadata
{
    private static final int SEND_TIME_OFFSET = 0;
    private static final int CORRELATION_ID_OFFSET = 8;
    private static final int CORRELATION_ID_LENGTH = 8;

    private final Options options = new Options();
    private MutableDirectBuffer buffer;
    private DirectBuffer readBuffer;
    private int offset = 0;

    public Metadata wrap(final MutableDirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.readBuffer = buffer;
        this.offset = offset;
        options.wrap(buffer, offset);
        return this;
    }

    public Metadata wrap(final DirectBuffer buffer, final int offset)
    {
        this.buffer = null;
        this.readBuffer = buffer;
        this.offset = offset;
        options.wrap(buffer, offset);
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
        buffer.putLong(offset + options.length() + SEND_TIME_OFFSET, value);
        return this;
    }

    public long originalTimestampNs()
    {
        return readBuffer.getLong(offset + options.length() + SEND_TIME_OFFSET);
    }

    public Metadata correlationId(final long value)
    {
        buffer.putLong(offset + options.length() + CORRELATION_ID_OFFSET, value);
        return this;
    }

    public long correlationId()
    {
        return readBuffer.getLong(offset + options.length() + CORRELATION_ID_OFFSET);
    }

    public int length()
    {
        return options.length() + CORRELATION_ID_OFFSET + CORRELATION_ID_LENGTH;
    }
}
