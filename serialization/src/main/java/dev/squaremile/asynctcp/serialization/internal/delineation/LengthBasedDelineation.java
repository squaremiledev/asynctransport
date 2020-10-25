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
    private boolean readingLength;
    private short undeliveredLength;
    private int currentMessageLength;
    private int currentMessagePadding;

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
        this.readingLength = lengthEncoding != LengthEncoding.FIXED_LENGTH;
        this.currentMessagePadding = fixedMessagePadding;
        this.currentMessageLength = lengthEncoding == LengthEncoding.FIXED_LENGTH ? fixedMessageLength : lengthEncoding.lengthFieldLength;
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
                previousDelivered = (short)(currentMessageLength - undeliveredLength);
                if (readingLength)
                {
                    currentMessageLength = lengthEncoding.readLength(undeliveredBuffer, 0);
                    currentMessagePadding = 0;
                    readingLength = false;
                }
                else
                {
                    delineatedDataHandler.onData(undeliveredBuffer, 0, currentMessageLength);
                    if (lengthEncoding != LengthEncoding.FIXED_LENGTH)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                        currentMessagePadding = fixedMessagePadding;
                        readingLength = true;
                    }
                }
            }
        }

        short pos = 0;
        for (int i = previousDelivered; i < length; i++)
        {
            pos++;
            if (pos == currentMessagePadding + currentMessageLength)
            {
                int currentOffset = offset + i - currentMessageLength + 1;
                if (readingLength)
                {
                    currentMessageLength = lengthEncoding.readLength(buffer, currentOffset);
                    currentMessagePadding = 0;
                    readingLength = false;
                }
                else
                {
                    delineatedDataHandler.onData(buffer, currentOffset, currentMessageLength);
                    if (lengthEncoding != LengthEncoding.FIXED_LENGTH)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                        currentMessagePadding = fixedMessagePadding;
                        readingLength = true;
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
}
