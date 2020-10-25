package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteOrder;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

class LengthBasedDelineation implements DelineationHandler
{
    private final int value;
    private final DelineationHandler delineatedDataHandler;
    private final MutableDirectBuffer undeliveredBuffer;
    private final LengthEncoding lengthEncoding;
    private Mode mode;
    private int currentMessageLength;
    private short undeliveredLength;

    LengthBasedDelineation(final LengthEncoding lengthEncoding, final int value, final DelineationHandler delineatedDataHandler)
    {
        this.lengthEncoding = lengthEncoding;
        this.value = value;
        this.delineatedDataHandler = delineatedDataHandler;
        this.undeliveredBuffer = createUndeliveredBuffer(lengthEncoding, value);
        this.undeliveredLength = 0;
        this.mode = Mode.READING_DATA;
    }

    private static UnsafeBuffer createUndeliveredBuffer(final LengthEncoding lengthEncoding, final int value)
    {
        switch (lengthEncoding)
        {
            case FIXED_LENGTH:
                return new UnsafeBuffer(new byte[value]);
            case INT_BIG_ENDIAN_FIELD:
                return new UnsafeBuffer(new byte[1024]); // TODO: probably not enough
            default:
                throw new UnsupportedOperationException(lengthEncoding.name());
        }
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        if (lengthEncoding == LengthEncoding.FIXED_LENGTH)
        {
            mode = Mode.READING_DATA;
            currentMessageLength = value;
        }
        else if (lengthEncoding == LengthEncoding.INT_BIG_ENDIAN_FIELD)
        {
            mode = Mode.READING_LENGTH;
            currentMessageLength = Integer.BYTES;
        }

        short previousDelivered = 0;
        if (undeliveredLength > 0)
        {
            if (undeliveredLength + length < currentMessageLength)
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, length);
                undeliveredLength += length;
                return;
            }
            else
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, currentMessageLength - undeliveredLength);
                delineatedDataHandler.onData(undeliveredBuffer, 0, currentMessageLength);
                previousDelivered = (short)(currentMessageLength - undeliveredLength);
            }
        }

        short pos = 0;
        for (int i = previousDelivered; i < length; i++)
        {
            pos++;
            if (pos == currentMessageLength)
            {
                if (mode == Mode.READING_LENGTH)
                {
                    currentMessageLength = buffer.getInt(offset + i - currentMessageLength + 1, lengthEncoding.byteOrder);
                    mode = Mode.READING_DATA;
                }
                else if (mode == Mode.READING_DATA)
                {
                    delineatedDataHandler.onData(buffer, offset + i - currentMessageLength + 1, currentMessageLength);
                    if (lengthEncoding != LengthEncoding.FIXED_LENGTH)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                        mode = Mode.READING_LENGTH;
                    }
                }
                pos = 0;
            }
        }
        undeliveredLength = pos;
        if (undeliveredLength > 0)
        {
            buffer.getBytes(offset + (length - undeliveredLength), undeliveredBuffer, 0, undeliveredLength);
        }
    }

    enum LengthEncoding
    {
        FIXED_LENGTH(ByteOrder.nativeOrder(), 0),
        INT_BIG_ENDIAN_FIELD(ByteOrder.BIG_ENDIAN, Integer.BYTES);

        private final ByteOrder byteOrder;
        private final int lengthFieldLength;

        LengthEncoding(final ByteOrder byteOrder, final int lengthFieldLength)
        {
            this.byteOrder = byteOrder;
            this.lengthFieldLength = lengthFieldLength;
        }
    }

    enum Mode
    {
        READING_LENGTH,
        READING_DATA
    }
}
