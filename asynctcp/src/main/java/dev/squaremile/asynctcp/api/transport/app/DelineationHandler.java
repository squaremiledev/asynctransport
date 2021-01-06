package dev.squaremile.asynctcp.api.transport.app;

import org.agrona.DirectBuffer;

public interface DelineationHandler
{
    void onData(final DirectBuffer buffer, final int offset, final int length);
}
