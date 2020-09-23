package dev.squaremile.asynctcp.api.app;

import dev.squaremile.asynctcp.api.values.CommandId;
import dev.squaremile.asynctcp.api.values.TransportId;

public interface TransportCommand extends CommandId, TransportId
{
    TransportCommand copy();
}
