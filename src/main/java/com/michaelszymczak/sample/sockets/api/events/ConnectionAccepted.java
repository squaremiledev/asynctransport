package com.michaelszymczak.sample.sockets.api.events;

public class ConnectionAccepted implements TransportEvent
{
    private final int port;
    private final long commandId;

    public ConnectionAccepted(final int port, final long commandId)
    {
        this.port = port;
        this.commandId = commandId;
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
        return "ConnectionAccepted{" +
               "port=" + port +
               ", commandId=" + commandId +
               '}';
    }
}
