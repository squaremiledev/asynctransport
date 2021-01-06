package dev.squaremile.asynctcp.api.transport.events;

import dev.squaremile.asynctcp.api.transport.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportEvent;

public class StoppedListening implements TransportCorrelatedEvent
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
    public boolean occursInSteadyState()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return "StoppedListening{" +
               "port=" + port +
               ", commandId=" + commandId +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new StoppedListening(port, commandId);
    }
}
