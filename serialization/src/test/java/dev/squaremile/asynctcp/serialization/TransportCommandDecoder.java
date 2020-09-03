package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;

public interface TransportCommandDecoder
{
    TransportCommand decode(DirectBuffer buffer, int offset);
}
