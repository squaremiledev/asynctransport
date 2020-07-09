package com.michaelszymczak.sample.sockets.api.events;

public class ConnectionAccepted implements TransportEvent
{
    private final int port;
    private final long commandId;
    private final int remotePort;

    public ConnectionAccepted(final int port, final long commandId, final int remotePort)
    {
        this.port = port;
        this.commandId = commandId;
        this.remotePort = remotePort;
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

    public int remotePort()
    {
        return remotePort;
    }

    @Override
    public String toString()
    {
        return "ConnectionAccepted{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", remotePort=" + remotePort +
               '}';
    }
}
