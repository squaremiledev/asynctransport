package com.michaelszymczak.sample.sockets.events;

public class CommandFailed implements TransportEvent
{
    private final int port;
    private final long commandId;
    private final String details;

    public CommandFailed(final int port, final long commandId, final String details)
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
    public String toString()
    {
        return "CommandFailed{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", details='" + details + '\'' +
               '}';
    }
}
