package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.TransportEvent;

public class ConnectionEstablished implements TransportEvent
{
    private final int port;
    private final long commandId;

    public ConnectionEstablished(final int port, final long commandId)
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
        return "ConnectionEstablished{" +
               "port=" + port +
               ", commandId=" + commandId +
               '}';
    }
}
