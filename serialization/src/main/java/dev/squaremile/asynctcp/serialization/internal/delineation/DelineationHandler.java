package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;

public interface DelineationHandler
{
    void onData(final DirectBuffer buffer, final int offset, final int length);
}
