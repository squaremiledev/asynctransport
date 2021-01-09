package dev.squaremile.tcpprobe;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class Options
{
    public static final int PLEASE_RESPOND_FLAG = 1;
    public static final int DO_NOT_RESPOND_FLAG = 0;

    private MutableDirectBuffer buffer;
    private DirectBuffer readBuffer;
    private int offset = 0;

    public Options wrap(final MutableDirectBuffer buffer, final int offset)
    {
        this.buffer = buffer;
        this.readBuffer = buffer;
        this.offset = offset;
        return this;
    }

    public Options wrap(final DirectBuffer buffer, final int offset)
    {
        this.buffer = null;
        this.readBuffer = buffer;
        this.offset = offset;
        return this;
    }

    public Options respond(final boolean value)
    {
        buffer.putInt(offset, value ? PLEASE_RESPOND_FLAG : DO_NOT_RESPOND_FLAG);
        return this;
    }

    public boolean respond()
    {
        return readBuffer.getInt(offset) == PLEASE_RESPOND_FLAG;
    }
}
