package dev.squaremile.asynctcp.domain.api.events;

import dev.squaremile.asynctcp.domain.api.CommandId;

public interface TransportCorrelatedEvent extends TransportEvent, CommandId
{
}
