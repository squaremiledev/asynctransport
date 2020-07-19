package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.ConnectionId;

public class ConnectionAccepted implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final int remotePort;
    private final long connectionId;
    private final int maxMsgSize;

    public ConnectionAccepted(final ConnectionId connectionId, final long commandId, final int remotePort, final int maxMsgSize)
    {
        this(connectionId.port(), commandId, remotePort, connectionId.connectionId(), maxMsgSize);
    }

    public ConnectionAccepted(final int port, final long commandId, final int remotePort, final long connectionId, final int maxMsgSize)
    {
        this.port = port;
        this.commandId = commandId;
        this.remotePort = remotePort;
        this.connectionId = connectionId;
        this.maxMsgSize = maxMsgSize;
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

    public int sendBufferSize()
    {
        return maxMsgSize;
    }

    @Override
    public String toString()
    {
        return "ConnectionAccepted{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", remotePort=" + remotePort +
               ", connectionId=" + connectionId +
               ", maxMsgSize=" + maxMsgSize +
               '}';
    }
}
