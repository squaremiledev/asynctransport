package dev.squaremile.asynctcp.api.app;

import dev.squaremile.asynctcp.api.values.TransportId;

public interface TransportEvent extends TransportId, Event
{
    @Override
    TransportEvent copy();
}
