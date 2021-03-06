package dev.squaremile.asynctcp.api.transport.events;

import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

public class Connected implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final String remoteHost;
    private final int remotePort;
    private final long connectionId;
    private final int inboundPduLimit;
    private final int outboundPduLimit;
    private final Delineation delineation;

    public Connected(
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
        this.remotePort = remotePort;
        this.remoteHost = remoteHost;
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

    public int remotePort()
    {
        return remotePort;
    }

    public String remoteHost()
    {
        return remoteHost;
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

    public Delineation delineation()
    {
        return delineation;
    }

    @Override
    public boolean occursInSteadyState()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return "Connected{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", remoteHost='" + remoteHost + '\'' +
               ", remotePort=" + remotePort +
               ", connectionId=" + connectionId +
               ", delineation=" + delineation +
               ", inboundPduLimit=" + inboundPduLimit +
               ", outboundPduLimit=" + outboundPduLimit +
               '}';
    }

    @Override
    public Connected copy()
    {
        return new Connected(port, commandId, remoteHost, remotePort, connectionId, inboundPduLimit, outboundPduLimit, delineation);
    }
}
