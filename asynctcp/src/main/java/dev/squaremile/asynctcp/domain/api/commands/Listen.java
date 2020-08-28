package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class Listen implements TransportCommand
{
    private int port = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;

    public Listen set(final int commandId, final int port)
    {
        this.port = port;
        this.commandId = commandId;
        return this;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }
}