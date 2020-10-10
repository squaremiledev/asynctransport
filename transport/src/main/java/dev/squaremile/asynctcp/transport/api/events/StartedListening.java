package dev.squaremile.asynctcp.transport.api.events;

import dev.squaremile.asynctcp.transport.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class StartedListening implements TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final Delineation delineation;

    public StartedListening(final int port, final long commandId, final Delineation delineation)
    {
        this.port = port;
        this.commandId = commandId;
        this.delineation = delineation;
    }

    @Override
    public int port()
    {
        return port;
    }

    public Delineation delineation()
    {
        return delineation;
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
        return "StartedListening{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", delineation=" + delineation +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new StartedListening(port, commandId, delineation);
    }
}
