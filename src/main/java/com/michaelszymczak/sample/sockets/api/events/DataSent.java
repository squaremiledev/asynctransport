package com.michaelszymczak.sample.sockets.api.events;

public class DataSent implements ConnectionEvent
{
    private final int port;
    private final long connectionId;

    public DataSent(final int port, final long connectionId)
    {
        this.port = port;
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
        return -1;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    @Override
    public String toString()
    {
        return "ConnectionAccepted{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               '}';
    }
}
