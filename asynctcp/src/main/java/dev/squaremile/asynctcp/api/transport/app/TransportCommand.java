package dev.squaremile.asynctcp.api.transport.app;

import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.api.transport.values.TransportId;

public interface TransportCommand extends CommandId, TransportId
{
    TransportCommand copy();
}
