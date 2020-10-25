package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

class LengthBasedDelineation implements DelineationHandler
{
    private final int fixedMessagePadding;
    private final int fixedMessageLength;
    private final DelineationHandler delineatedDataHandler;
    private final MutableDirectBuffer undeliveredBuffer;
    private final LengthEncoding lengthEncoding;
    private Mode mode;
    private short undeliveredLength;

    LengthBasedDelineation(
            final LengthEncoding lengthEncoding,
            final int fixedMessagePadding,
            final int fixedMessageLength,
            final DelineationHandler delineatedDataHandler
    )
    {
        this.fixedMessagePadding = fixedMessagePadding;
        this.lengthEncoding = lengthEncoding;
        this.fixedMessageLength = fixedMessageLength;
        this.delineatedDataHandler = delineatedDataHandler;
        this.undeliveredBuffer = createUndeliveredBuffer(lengthEncoding, fixedMessageLength);
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
        int currentMessageLength;
        int currentMessagePadding = fixedMessagePadding;
        if (lengthEncoding == LengthEncoding.FIXED_LENGTH)
        {
            mode = Mode.READING_DATA;
            currentMessageLength = fixedMessageLength;
        }
        else
        {
            mode = Mode.READING_LENGTH;
            currentMessageLength = lengthEncoding.lengthFieldLength;
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
            if (pos == currentMessagePadding + currentMessageLength)
            {
                int currentOffset = offset + i - currentMessageLength + 1;
                if (mode == Mode.READING_LENGTH)
                {
                    currentMessageLength = lengthEncoding.readLength(buffer, currentOffset);
                    currentMessagePadding = 0;
                    mode = Mode.READING_DATA;
                }
                else if (mode == Mode.READING_DATA)
                {
                    delineatedDataHandler.onData(buffer, currentOffset, currentMessageLength);
                    if (lengthEncoding != LengthEncoding.FIXED_LENGTH)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                        currentMessagePadding = fixedMessagePadding;
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

    enum Mode
    {
        READING_LENGTH,
        READING_DATA
    }
}
