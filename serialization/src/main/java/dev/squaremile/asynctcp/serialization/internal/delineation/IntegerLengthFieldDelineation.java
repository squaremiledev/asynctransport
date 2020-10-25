package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;

class IntegerLengthFieldDelineation implements DelineationHandler
{
    private final DelineationHandler delineation;

    IntegerLengthFieldDelineation(final DelineationHandler delineatedDataHandler)
    {
        this.delineation = new LengthBasedDelineation(LengthBasedDelineation.LengthEncoding.INT_BIG_ENDIAN_FIELD, 0, delineatedDataHandler);
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        delineation.onData(buffer, offset, length);
    }
}
