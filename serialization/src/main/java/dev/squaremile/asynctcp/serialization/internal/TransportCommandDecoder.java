package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.TransportCommand;

public interface TransportCommandDecoder
{
    TransportCommand decode(DirectBuffer buffer, int offset);
}
