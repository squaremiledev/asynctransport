package dev.squaremile.asynctcp.transport.api.values;

import org.agrona.DirectBuffer;


import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public enum LengthEncoding
{
    FIXED_LENGTH(0, (buffer, currentOffset) -> 0),
    SHORT_BIG_ENDIAN_FIELD(Short.BYTES, (buffer, currentOffset) -> buffer.getShort(currentOffset, BIG_ENDIAN)),
    SHORT_LITTLE_ENDIAN_FIELD(Short.BYTES, (buffer, currentOffset) -> buffer.getShort(currentOffset, LITTLE_ENDIAN)),
    INT_BIG_ENDIAN_FIELD(Integer.BYTES, (buffer, currentOffset) -> buffer.getInt(currentOffset, BIG_ENDIAN)),
    INT_LITTLE_ENDIAN_FIELD(Integer.BYTES, (buffer, currentOffset) -> buffer.getInt(currentOffset, LITTLE_ENDIAN));

    public final int lengthFieldLength;
    private final LengthProvider lengthProvider;

    LengthEncoding(final int lengthFieldLength, final LengthProvider lengthProvider)
    {
        this.lengthFieldLength = lengthFieldLength;
        this.lengthProvider = lengthProvider;
    }

    public int readLength(final DirectBuffer buffer, final int currentOffset)
    {
        return lengthProvider.readLength(buffer, currentOffset);
    }

    interface LengthProvider
    {
        int readLength(final DirectBuffer buffer, final int currentOffset);
    }
}
