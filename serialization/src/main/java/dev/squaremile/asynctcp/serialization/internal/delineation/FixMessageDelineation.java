package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;

public class FixMessageDelineation implements DelineationHandler
{
    private final DelineationHandler delineatedDataHandler;

    public FixMessageDelineation(final DelineationHandler delineatedDataHandler)
    {
        this.delineatedDataHandler = delineatedDataHandler;
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        if (length > 0)
        {
            delineatedDataHandler.onData(buffer, offset, length);
        }
    }
}
