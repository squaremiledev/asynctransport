package dev.squaremile.asynctcp.api.transport.events;

import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportEvent;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

public class ConnectionAccepted implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final String remoteHost;
    private final int remotePort;
    private final long connectionId;
    private final int inboundPduLimit;
    private final int outboundPduLimit;
    private final Delineation delineation;

    public ConnectionAccepted(
            final ConnectionId connectionId,
            final long commandId,
            final String remoteHost,
            final int remotePort,
            final int inboundPduLimit,
            final int outboundPduLimit,
            final Delineation delineation
    )
    {
        this(connectionId.port(), commandId, remoteHost, remotePort, connectionId.connectionId(), inboundPduLimit, outboundPduLimit, delineation);
    }

    public ConnectionAccepted(
            final int port,
            final long commandId,
            final String remoteHost,
            final int remotePort,
            final long connectionId,
            final int inboundPduLimit,
            final int outboundPduLimit,
            final Delineation delineation
    )
    {
        this.port = port;
        this.commandId = commandId;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connectionId = connectionId;
        this.inboundPduLimit = inboundPduLimit;
        this.outboundPduLimit = outboundPduLimit;
        this.delineation = delineation;
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

    public String remoteHost()
    {
        return remoteHost;
    }

    public int remotePort()
    {
        return remotePort;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    public int inboundPduLimit()
    {
        return inboundPduLimit;
    }

    public int outboundPduLimit()
    {
        return outboundPduLimit;
    }

    @Override
    public boolean occursInSteadyState()
    {
        return false;
    }

    public Delineation delineation()
    {
        return delineation;
    }

    @Override
    public String toString()
    {
        return "ConnectionAccepted{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", remoteHost='" + remoteHost + '\'' +
               ", remotePort=" + remotePort +
               ", connectionId=" + connectionId +
               ", inboundPduLimit=" + inboundPduLimit +
               ", outboundPduLimit=" + outboundPduLimit +
               ", delineation=" + delineation +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new ConnectionAccepted(port, commandId, remoteHost, remotePort, connectionId, inboundPduLimit, outboundPduLimit, delineation);
    }
}
