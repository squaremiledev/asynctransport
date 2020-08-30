package dev.squaremile.asynctcp.domain.connection;

import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;

public class ConnectionConfiguration
{
    public final ConnectionIdValue connectionId;
    public final String remoteHost;
    public final int remotePort;
    public final int outboundPduLimit;
    public final int sendBufferSize;
    public final int inboundPduLimit;

    public ConnectionConfiguration(
            final ConnectionIdValue connectionId,
            final String remoteHost,
            final int remotePort,
            final int outboundPduLimit,
            final int sendBufferSize,
            final int inboundPduLimit
    )
    {
        this.connectionId = connectionId;
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
