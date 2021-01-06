package dev.squaremile.asynctcp.api.transport.app;

import dev.squaremile.asynctcp.api.transport.values.CommandId;

public interface TransportCorrelatedEvent extends TransportEvent, CommandId
{
}
