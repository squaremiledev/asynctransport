package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;

class IntegersDelineation implements DelineationHandler
{
    private final FixedLengthDelineation delineation;

    IntegersDelineation(final DelineationHandler delineatedDataHandler)
    {
        this.delineation = new FixedLengthDelineation(delineatedDataHandler, 4);
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        delineation.onData(buffer, offset, length);
    }
}
