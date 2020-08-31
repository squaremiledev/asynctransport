package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.ConnectionId;

public class CloseConnection implements ConnectionCommand
{
    private final int port;
    private final long connectionId;
    private long commandId = CommandId.NO_COMMAND_ID;

    public CloseConnection(final ConnectionId connectionId)
    {
        this.port = connectionId.port();
        this.connectionId = connectionId.connectionId();
    }

    public CloseConnection set(final long commandId)
    {
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

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    @Override
    public String toString()
    {
        return "CloseConnection{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", commandId=" + commandId +
               '}';
    }
}
