package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.TransportEvent;

public class StoppedListening implements TransportEvent
{
    private final int port;
    private final long commandId;

    public StoppedListening(final int port, final long commandId)
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
        return "StoppedListening{" +
               "port=" + port +
               ", commandId=" + commandId +
               '}';
    }

}
