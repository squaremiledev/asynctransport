package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

class LongsDelineation implements DelineationHandler
{
    private final DelineationHandler delineatedDataHandler;
    private final MutableDirectBuffer undeliveredBuffer = new UnsafeBuffer(new byte[8]);
    private short undeliveredLength = 0;


    LongsDelineation(final DelineationHandler delineatedDataHandler)
    {
        this.delineatedDataHandler = delineatedDataHandler;
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        short previousDelivered = 0;
        if (undeliveredLength > 0)
        {
            if (undeliveredLength + length < 8)
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, length);
                undeliveredLength += length;
                return;
            }
            else
            {
                buffer.getBytes(offset, undeliveredBuffer, undeliveredLength, 8 - undeliveredLength);
                delineatedDataHandler.onData(undeliveredBuffer, 0, 8);
                previousDelivered = (short)(8 - undeliveredLength);
            }
        }

        short pos = 0;
        for (int i = previousDelivered; i < length; i++)
        {
            pos++;
            if (pos == 8)
            {
                delineatedDataHandler.onData(buffer, offset + i - 7, 8);
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
