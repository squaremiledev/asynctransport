package dev.squaremile.asynctcp.transport.api.app;

import dev.squaremile.asynctcp.transport.api.values.TransportId;

public interface TransportEvent extends TransportId, Event
{
    @Override
    TransportEvent copy();
}
