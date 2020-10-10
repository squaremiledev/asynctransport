package dev.squaremile.asynctcp.transport.api.events;

import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;

public class ConnectionClosed implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final long connectionId;

    public ConnectionClosed(final int port, final long connectionId, final long commandId)
    {
        this.port = port;
        this.commandId = commandId;
        this.connectionId = connectionId;
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
    public long connectionId()
    {
        return connectionId;
    }

    @Override
    public boolean occursInSteadyState()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return "ConnectionClosed{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", connectionId=" + connectionId +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new ConnectionClosed(port, connectionId, commandId);
    }
}
