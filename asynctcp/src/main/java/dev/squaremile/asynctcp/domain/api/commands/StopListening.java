package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class StopListening implements TransportCommand
{
    private long commandId = CommandId.NO_COMMAND_ID;
    private int port = TransportId.NO_PORT;

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

    public StopListening set(final long commandId, final int port)
    {
        this.commandId = commandId;
        this.port = port;
        return this;
    }
}
