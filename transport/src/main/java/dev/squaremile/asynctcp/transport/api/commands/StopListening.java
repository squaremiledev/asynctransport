package dev.squaremile.asynctcp.transport.api.commands;

import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.TransportId;

public class StopListening implements TransportUserCommand
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

    @Override
    public String toString()
    {
        return "StopListening{" +
               "commandId=" + commandId +
               ", port=" + port +
               '}';
    }

    @Override
    public StopListening copy()
    {
        return new StopListening().set(commandId, port);
    }
}
