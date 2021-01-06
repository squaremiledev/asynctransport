package dev.squaremile.asynctcp.api.transport.app;

import dev.squaremile.asynctcp.api.transport.values.TransportId;

public interface TransportEvent extends TransportId, Event
{
    @Override
    TransportEvent copy();
}
