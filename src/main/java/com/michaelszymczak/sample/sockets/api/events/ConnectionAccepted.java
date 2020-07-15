package com.michaelszymczak.sample.sockets.api.events;

public class ConnectionAccepted implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final int remotePort;
    private final long connectionId;
    private final int sendBufferSize;

    public ConnectionAccepted(final int port, final long commandId, final int remotePort, final long connectionId, final int sendBufferSize)
    {
        this.port = port;
        this.commandId = commandId;
        this.remotePort = remotePort;
        this.connectionId = connectionId;
        this.sendBufferSize = sendBufferSize;
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
        return sendBufferSize;
    }

    @Override
    public String toString()
    {
        return "ConnectionAccepted{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", remotePort=" + remotePort +
               ", connectionId=" + connectionId +
               ", sendBufferSize=" + sendBufferSize +
               '}';
    }
}
