package dev.squaremile.asynctcp.transport.api.app;

import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.TransportId;

public interface TransportCommand extends CommandId, TransportId
{
    TransportCommand copy();
}
