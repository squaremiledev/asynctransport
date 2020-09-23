package dev.squaremile.asynctcp.internal.domain.connection;

import dev.squaremile.asynctcp.api.values.ConnectionId;
import dev.squaremile.asynctcp.api.values.ConnectionIdValue;

public class ConnectionConfiguration
{
    public final ConnectionIdValue connectionId;
    public final String remoteHost;
    public final int remotePort;
    public final int outboundPduLimit;
    public final int sendBufferSize;
    public final int inboundPduLimit;

    public ConnectionConfiguration(
            final ConnectionId connectionId,
            final String remoteHost,
            final int remotePort,
            final int outboundPduLimit,
            final int sendBufferSize,
            final int inboundPduLimit
    )
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.outboundPduLimit = outboundPduLimit;
        this.sendBufferSize = sendBufferSize;
        this.inboundPduLimit = inboundPduLimit;
    }

    @Override
    public String toString()
    {
        return "ConnectionConfiguration{" +
               "connectionId=" + connectionId +
               ", remoteHost='" + remoteHost + '\'' +
               ", remotePort=" + remotePort +
               ", outboundPduLimit=" + outboundPduLimit +
               ", sendBufferSize=" + sendBufferSize +
               ", inboundPduLimit=" + inboundPduLimit +
               '}';
    }
}
