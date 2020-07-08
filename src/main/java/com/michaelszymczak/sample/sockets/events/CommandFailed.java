package com.michaelszymczak.sample.sockets.events;

public class CommandFailed implements TransportEvent
{
    private final long commandId;
    private final int port;
    private final String details;

    public CommandFailed(final long commandId, final int port, final String details)
    {
        this.commandId = commandId;
        this.port = port;
        this.details = details;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public int port()
    {
        return port;
    }
}
