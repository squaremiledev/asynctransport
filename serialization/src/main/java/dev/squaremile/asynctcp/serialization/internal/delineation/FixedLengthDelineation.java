package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

class FixedLengthDelineation implements DelineationHandler
{
    private final int fixedMessageLength;
    private final DelineationHandler delineatedDataHandler;
    private final MutableDirectBuffer undeliveredBuffer;
    private short undeliveredLength;


    FixedLengthDelineation(final DelineationHandler delineatedDataHandler, final int fixedMessageLength)
    {
        this.delineatedDataHandler = delineatedDataHandler;
        this.fixedMessageLength = fixedMessageLength;
        this.undeliveredBuffer = new UnsafeBuffer(new byte[fixedMessageLength]);
        this.undeliveredLength = 0;
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        short previousDelivered = 0;
        if (undeliveredLength > 0)
        {
            if (undeliveredLength + length < fixedMessageLength)
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, length);
                undeliveredLength += length;
                return;
            }
            else
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, fixedMessageLength - undeliveredLength);
                delineatedDataHandler.onData(undeliveredBuffer, 0, fixedMessageLength);
                previousDelivered = (short)(fixedMessageLength - undeliveredLength);
            }
        }

        short pos = 0;
        for (int i = previousDelivered; i < length; i++)
        {
            pos++;
            if (pos == fixedMessageLength)
            {
                delineatedDataHandler.onData(buffer, offset + i - fixedMessageLength + 1, fixedMessageLength);
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
