package dev.squaremile.asynctcp.internal.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.transport.app.TransportCommand;

public interface TransportCommandDecoder
{
    TransportCommand decode(DirectBuffer buffer, int offset);
}
