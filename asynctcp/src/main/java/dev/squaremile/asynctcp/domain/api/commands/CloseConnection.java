package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;

public class CloseConnection implements ConnectionCommand
{
    private long commandId = CommandId.NO_COMMAND_ID;
    private final ConnectionId connectionId;

    public CloseConnection(final ConnectionId connectionId)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
    }

    public CloseConnection set(final long commandId)
    {
        this.commandId = commandId;
        return this;
    }

    @Override
    public int port()
    {
        return connectionId.port();
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public long connectionId()
    {
        return connectionId.connectionId();
    }

    @Override
    public String toString()
    {
        return "CloseConnection{" +
               ", connectionId=" + connectionId +
               ", commandId=" + commandId +
               '}';
    }

    @Override
    public CloseConnection copy()
    {
        return new CloseConnection(connectionId).set(commandId);
    }
}
