package dev.squaremile.asynctcp.transport.api.app;

import dev.squaremile.asynctcp.transport.api.values.CommandId;

public interface TransportCorrelatedEvent extends TransportEvent, CommandId
{
}
