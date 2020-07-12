package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;

public class CommandFailed implements TransportEvent, CorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final String details;

    public CommandFailed(final TransportCommand command, final String details)
    {
        this.port = command.port();
        this.commandId = command.commandId();
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
        return "CommandFailed{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", details='" + details + '\'' +
               '}';
    }
}
