package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.TransportEvent;

public interface TransportEventDecoder
{
    TransportEvent decode(DirectBuffer buffer, int offset, final int length);
}
