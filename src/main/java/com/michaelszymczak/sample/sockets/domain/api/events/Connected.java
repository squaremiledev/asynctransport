package com.michaelszymczak.sample.sockets.domain.api.events;

public class Connected implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final int remotePort;
    private final long connectionId;
    private final int maxInboundMessageSize;
    private final int maxOutboundMessageSize;

    public Connected(
            final int port,
            final long commandId,
            final int remotePort,
            final long connectionId,
            final int maxInboundMessageSize,
            final int maxOutboundMessageSize
    )
    {
        this.port = port;
        this.commandId = commandId;
        this.remotePort = remotePort;
        this.connectionId = connectionId;
        this.maxInboundMessageSize = maxInboundMessageSize;
        this.maxOutboundMessageSize = maxOutboundMessageSize;
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

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    public int maxInboundMessageSize()
    {
        return maxInboundMessageSize;
    }

    public int maxOutboundMessageSize()
    {
        return maxOutboundMessageSize;
    }

    @Override
    public String toString()
    {
        return "Connected{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", remotePort=" + remotePort +
               ", connectionId=" + connectionId +
               ", maxInboundMessageSize=" + maxInboundMessageSize +
               ", maxOutboundMessageSize=" + maxOutboundMessageSize +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new Connected(port, commandId, remotePort, connectionId, maxInboundMessageSize, maxOutboundMessageSize);
    }
}
