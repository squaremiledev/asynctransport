package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionIdValue;

public class ConnectionConfiguration
{
    public final ConnectionIdValue connectionId;
    public final int remotePort;
    public final int maxOutboundMessageSize;
    public final int sendBufferSize;
    public final int maxInboundMessageSize;

    public ConnectionConfiguration(final ConnectionIdValue connectionId, final int remotePort, final int maxOutboundMessageSize, final int sendBufferSize, final int maxInboundMessageSize)
    {
        this.connectionId = connectionId;
        this.remotePort = remotePort;
        this.maxOutboundMessageSize = maxOutboundMessageSize;
        this.sendBufferSize = sendBufferSize;
        this.maxInboundMessageSize = maxInboundMessageSize;
    }
}
