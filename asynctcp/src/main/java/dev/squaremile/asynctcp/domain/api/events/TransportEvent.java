package dev.squaremile.asynctcp.domain.api.events;

import dev.squaremile.asynctcp.domain.api.TransportId;

public interface TransportEvent extends TransportId, Event
{
    TransportEvent copy();
}
