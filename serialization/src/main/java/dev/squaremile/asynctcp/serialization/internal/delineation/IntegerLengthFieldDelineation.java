package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;


import static dev.squaremile.asynctcp.serialization.internal.delineation.LengthEncoding.INT_BIG_ENDIAN_FIELD;

class IntegerLengthFieldDelineation implements DelineationHandler
{
    private final DelineationHandler delineation;

    IntegerLengthFieldDelineation(final DelineationHandler delineatedDataHandler, final int messagePadding)
    {
        this.delineation = new LengthBasedDelineation(INT_BIG_ENDIAN_FIELD, messagePadding, 0, delineatedDataHandler);
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        delineation.onData(buffer, offset, length);
    }
}
