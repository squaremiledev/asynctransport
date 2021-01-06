package dev.squaremile.asynctcp.internal.serialization.delineation;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;


import dev.squaremile.asynctcp.api.transport.values.Delineation;

class LengthBasedDelineation implements DelineationHandler
{
    private final int fixedMessagePadding;
    private final int fixedMessageLength;
    private final DelineationHandler delineatedDataHandler;
    private final MutableDirectBuffer undeliveredBuffer;
    private final Delineation.Type lengthEncoding;
    private boolean readingLength;
    private int undeliveredLength;
    private int currentMessageLength;
    private int currentMessagePadding;

    LengthBasedDelineation(
            final Delineation.Type lengthEncoding,
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
        this.readingLength = lengthEncoding != Delineation.Type.FIXED_LENGTH;
        this.currentMessagePadding = fixedMessagePadding;
        this.currentMessageLength = lengthEncoding == Delineation.Type.FIXED_LENGTH ? fixedMessageLength : lengthEncoding.lengthFieldLength;
    }

    private static UnsafeBuffer createUndeliveredBuffer(final Delineation.Type lengthEncoding, final int value)
    {
        switch (lengthEncoding)
        {
            case FIXED_LENGTH:
                return new UnsafeBuffer(new byte[value]);
            case SHORT_BIG_ENDIAN_FIELD:
            case SHORT_LITTLE_ENDIAN_FIELD:
                return new UnsafeBuffer(new byte[Short.MAX_VALUE]);
            case INT_BIG_ENDIAN_FIELD:
            case INT_LITTLE_ENDIAN_FIELD:
                // TODO #16 may not be enough
                return new UnsafeBuffer(new byte[Short.MAX_VALUE * 2]);
            default:
                throw new UnsupportedOperationException(lengthEncoding.name());
        }
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        short previousDelivered = deliverUndelivered(buffer, offset, length);
        if (previousDelivered >= 0)
        {
            storeInUndeliveredBuffer(
                    buffer, offset, length,
                    deliverCurrent(buffer, offset + previousDelivered, length - previousDelivered)
            );
        }
    }

    private short deliverUndelivered(final DirectBuffer buffer, final int offset, final int length)
    {
        short previousDelivered = 0;
        if (undeliveredLength > 0)
        {
            if (undeliveredLength + length < currentMessagePadding + currentMessageLength)
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, length);
                undeliveredLength += length;
                return -1;
            }
            else
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, currentMessageLength + currentMessagePadding - undeliveredLength);
                previousDelivered = (short)(currentMessageLength + currentMessagePadding - undeliveredLength);
                if (readingLength)
                {
                    currentMessageLength = lengthEncoding.readLength(undeliveredBuffer, currentMessagePadding) + fixedMessageLength;
                    if (currentMessageLength == 0)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                    }
                    else
                    {
                        currentMessagePadding = 0;
                        readingLength = false;
                    }
                }
                else
                {
                    delineatedDataHandler.onData(undeliveredBuffer, currentMessagePadding, currentMessageLength);
                    if (lengthEncoding != Delineation.Type.FIXED_LENGTH)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                        currentMessagePadding = fixedMessagePadding;
                        readingLength = true;
                    }
                }
            }
        }
        return previousDelivered;
    }

    private int deliverCurrent(final DirectBuffer buffer, final int offset, final int length)
    {
        int undelivered = 0;
        for (int i = 0; i < length; i++)
        {
            undelivered++;
            boolean chunkComplete = undelivered == currentMessagePadding + currentMessageLength;
            if (chunkComplete)
            {
                undelivered = 0;
                int currentOffset = offset + i - currentMessageLength + 1;
                if (readingLength)
                {
                    currentMessageLength = lengthEncoding.readLength(buffer, currentOffset) + fixedMessageLength;
                    if (currentMessageLength == 0)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                    }
                    else
                    {
                        currentMessagePadding = 0;
                        readingLength = false;
                    }
                }
                else
                {
                    delineatedDataHandler.onData(buffer, currentOffset, currentMessageLength);
                    if (lengthEncoding != Delineation.Type.FIXED_LENGTH)
                    {
                        currentMessageLength = lengthEncoding.lengthFieldLength;
                        currentMessagePadding = fixedMessagePadding;
                        readingLength = true;
                    }
                }
            }
        }
        return undelivered;
    }

    private void storeInUndeliveredBuffer(final DirectBuffer buffer, final int offset, final int length, final int undeliveredLength)
    {
        if (undeliveredLength > 0)
        {
            buffer.getBytes(offset + (length - undeliveredLength), undeliveredBuffer, 0, undeliveredLength);
        }
        this.undeliveredLength = undeliveredLength;
    }
}
