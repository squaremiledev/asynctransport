package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;

public class ConnectionCommandFailed implements ConnectionEvent, CorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final String details;
    private final long connectionId;

    public ConnectionCommandFailed(final ConnectionCommand command, final String details)
    {
        this.port = command.port();
        this.commandId = command.commandId();
        this.connectionId = command.connectionId();
        this.details = details;
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
}
