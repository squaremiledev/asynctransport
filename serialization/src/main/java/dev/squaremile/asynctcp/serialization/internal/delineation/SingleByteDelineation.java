package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;

class SingleByteDelineation implements DelineationHandler
{
    private final byte[] oneByteFlyweight = new byte[1];
    private final ByteBuffer oneByteByteBuffer = ByteBuffer.wrap(oneByteFlyweight);
    private final DelineationHandler delineatedDataHandler;

    SingleByteDelineation(final DelineationHandler delineatedDataHandler)
    {
        this.delineatedDataHandler = delineatedDataHandler;
    }

    @Override
    public void onData(final ByteBuffer byteBuffer, final int offset, final int length)
    {
        byteBuffer.position(offset).limit(offset + length);
        for (int i = 0; i < length; i++)
        {
            oneByteFlyweight[0] = byteBuffer.get(offset + i);
            delineatedDataHandler.onData(oneByteByteBuffer, 0, 1);
        }
    }
}
