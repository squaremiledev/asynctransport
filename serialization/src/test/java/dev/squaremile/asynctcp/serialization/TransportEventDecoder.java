package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.domain.api.events.TransportEvent;

public interface TransportEventDecoder
{
    TransportEvent decode(DirectBuffer buffer, int offset);
}
