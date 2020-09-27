package dev.squaremile.asynctcp.transport.api.events;

import dev.squaremile.asynctcp.transport.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;

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
