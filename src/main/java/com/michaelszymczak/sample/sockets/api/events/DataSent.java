package com.michaelszymczak.sample.sockets.api.events;

public class DataSent implements ConnectionEvent
{
    private final int port;
    private final long connectionId;
    private final long totalBytesSent;

    public DataSent(final int port, final long connectionId, final long totalBytesSent)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.totalBytesSent = totalBytesSent;
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

    public long totalBytesSent()
    {
        return totalBytesSent;
    }

    @Override
    public String toString()
    {
        return "DataSent{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", totalBytesSent=" + totalBytesSent +
               '}';
    }
}
