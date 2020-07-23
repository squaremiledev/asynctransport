package com.michaelszymczak.sample.sockets.domain.api.events;

import com.michaelszymczak.sample.sockets.domain.api.commands.TransportCommand;

public class TransportCommandFailed implements CommandFailed
{
    private final int port;
    private final long commandId;
    private final String details;

    public TransportCommandFailed(final TransportCommand command, final String details)
    {
        this(command.port(), command.commandId(), details);
    }

    public TransportCommandFailed(final int port, final long commandId, final String details)
    {
        this.port = port;
        this.commandId = commandId;
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

    @Override
    public String details()
    {
        return details;
    }

    @Override
    public String toString()
    {
        return "TransportCommandFailed{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", details='" + details + '\'' +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new TransportCommandFailed(port, commandId, details);
    }
}
