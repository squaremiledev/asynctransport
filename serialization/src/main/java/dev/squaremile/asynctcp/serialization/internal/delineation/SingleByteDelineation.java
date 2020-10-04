package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;

class SingleByteDelineation implements DelineationHandler
{
    private final DelineationHandler delineatedDataHandler;

    SingleByteDelineation(final DelineationHandler delineatedDataHandler)
    {
        this.delineatedDataHandler = delineatedDataHandler;
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        for (int i = 0; i < length; i++)
        {
            delineatedDataHandler.onData(buffer, offset + i, 1);
        }
    }
}
