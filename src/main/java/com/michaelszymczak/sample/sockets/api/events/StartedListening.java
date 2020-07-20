package com.michaelszymczak.sample.sockets.api.events;

public class StartedListening implements TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;

    public StartedListening(final int port, final long commandId)
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
        return "StartedListening{" +
               "port=" + port +
               ", commandId=" + commandId +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new StartedListening(port, commandId);
    }
}
