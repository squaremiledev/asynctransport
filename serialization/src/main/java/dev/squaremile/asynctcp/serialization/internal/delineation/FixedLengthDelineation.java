package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.transport.api.values.Delineation;

class FixedLengthDelineation implements DelineationHandler
{
    private final DelineationHandler delineation;

    FixedLengthDelineation(final DelineationHandler delineatedDataHandler, final int fixedMessageLength)
    {
        delineation = new LengthBasedDelineation(Delineation.Type.FIXED_LENGTH, 0, fixedMessageLength, delineatedDataHandler);
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        delineation.onData(buffer, offset, length);
    }
}
