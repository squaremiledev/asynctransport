package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class Connect implements TransportCommand
{
    private int port = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;

    public Connect set(final int port, final long commandId)
    {
        this.port = port;
        this.commandId = commandId;
        return this;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public int port()
    {
        return port;
    }
}
