package com.michaelszymczak.sample.sockets.api.events;

public class DataSent implements ConnectionEvent
{
    private final int port;
    private final long connectionId;
    private final int bytesSent;
    private final long totalBytesSent;

    public DataSent(final int port, final long connectionId, final int bytesSent, final long totalBytesSent)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.bytesSent = bytesSent;
        this.totalBytesSent = totalBytesSent;
    }

    @Override
    public int port()
    {
        return port;
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

    public int bytesSent()
    {
        return bytesSent;
    }

    @Override
    public String toString()
    {
        return "DataSent{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", bytesSent=" + bytesSent +
               ", totalBytesSent=" + totalBytesSent +
               '}';
    }
}
