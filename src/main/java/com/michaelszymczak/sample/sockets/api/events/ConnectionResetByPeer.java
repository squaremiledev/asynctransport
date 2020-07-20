package com.michaelszymczak.sample.sockets.api.events;

public class ConnectionResetByPeer implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final long connectionId;

    public ConnectionResetByPeer(final int port, final long connectionId, final long commandId)
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
    public String toString()
    {
        return "ConnectionResetByPeer{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", connectionId=" + connectionId +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new ConnectionResetByPeer(port, connectionId, commandId);
    }
}
