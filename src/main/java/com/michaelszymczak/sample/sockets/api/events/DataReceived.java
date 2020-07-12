package com.michaelszymczak.sample.sockets.api.events;

public class DataReceived implements ConnectionEvent
{
    private final int port;
    private final long connectionId;
    private final long totalBytesReceived;

    public DataReceived(final int port, final long connectionId, final long totalBytesReceived)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.totalBytesReceived = totalBytesReceived;
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
        return "DataSent{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", totalBytesReceived=" + totalBytesReceived +
               '}';
    }
}
