package dev.squaremile.asynctcp.domain.api.events;

import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;

public class ConnectionCommandFailed implements CommandFailed, ConnectionEvent
{
    private final int port;
    private final long commandId;
    private final String details;
    private final long connectionId;

    public ConnectionCommandFailed(final ConnectionCommand command, final String details)
    {
        this(command.port(), command.commandId(), details, command.connectionId());
    }

    public ConnectionCommandFailed(final int port, final long commandId, final String details, final long connectionId)
    {
        this.port = port;
        this.commandId = commandId;
        this.details = details;
        this.connectionId = connectionId;
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
    public String details()
    {
        return details;
    }

    @Override
    public String toString()
    {
        return "ConnectionCommandFailed{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", details='" + details + '\'' +
               ", connectionId=" + connectionId +
               '}';
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    @Override
    public ConnectionCommandFailed copy()
    {
        return new ConnectionCommandFailed(port, commandId, details, connectionId);
    }
}
