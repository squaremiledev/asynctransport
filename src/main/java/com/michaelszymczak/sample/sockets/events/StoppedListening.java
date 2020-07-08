package com.michaelszymczak.sample.sockets.events;

public class StoppedListening implements TransportEvent
{
    private final long commandId;
    private final int port;

    public StoppedListening(final long commandId, final int port)
    {
        this.commandId = commandId;
        this.port = port;
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

    @Override
    public String toString()
    {
        return "StartedListening{" +
               "commandId=" + commandId +
               ", port=" + port +
               '}';
    }

}
