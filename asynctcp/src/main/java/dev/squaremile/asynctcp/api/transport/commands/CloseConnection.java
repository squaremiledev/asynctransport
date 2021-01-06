package dev.squaremile.asynctcp.api.transport.commands;

import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;

public class CloseConnection implements ConnectionUserCommand
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
               "commandId=" + commandId +
               ", connectionId=" + connectionId +
               '}';
    }

    @Override
    public CloseConnection copy()
    {
        return new CloseConnection(connectionId).set(commandId);
    }
}
