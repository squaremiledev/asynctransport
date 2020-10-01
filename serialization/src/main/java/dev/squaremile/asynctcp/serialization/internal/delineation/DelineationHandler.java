package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;

public interface DelineationHandler
{
    void onData(final ByteBuffer byteBuffer, final int offset, final int length);
}
