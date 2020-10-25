package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;


import static java.nio.ByteOrder.BIG_ENDIAN;

enum LengthEncoding
{
    FIXED_LENGTH(0, (buffer, currentOffset) -> 0),
    INT_BIG_ENDIAN_FIELD(Integer.BYTES, (buffer, currentOffset) -> buffer.getInt(currentOffset, BIG_ENDIAN));

    final int lengthFieldLength;
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
