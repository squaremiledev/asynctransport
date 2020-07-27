package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionIdValue;

public class ConnectionConfiguration
{
    public final ConnectionIdValue connectionId;
    public final int remotePort;
    public final int outboundPduLimit;
    public final int sendBufferSize;
    public final int inboundPduLimit;

    public ConnectionConfiguration(final ConnectionIdValue connectionId, final int remotePort, final int outboundPduLimit, final int sendBufferSize, final int inboundPduLimit)
    {
        this.connectionId = connectionId;
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
               ", remotePort=" + remotePort +
               ", outboundPduLimit=" + outboundPduLimit +
               ", sendBufferSize=" + sendBufferSize +
               ", inboundPduLimit=" + inboundPduLimit +
               '}';
    }
}
