package dev.squaremile.asynctcp.internal.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.transport.app.TransportEvent;

public interface TransportEventDecoder
{
    TransportEvent decode(DirectBuffer buffer, int offset, final int length);
}
