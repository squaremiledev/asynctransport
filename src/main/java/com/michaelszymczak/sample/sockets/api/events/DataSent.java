package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionId;

public class DataSent implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long connectionId;
    private final int bytesSent;
    private final long totalBytesSent;
    private final long totalBytesBuffered;
    private final long commandId;

    public DataSent(final ConnectionId connectionId, final int bytesSent, final long totalBytesSent, final long totalBytesBuffered)
    {
        this(connectionId, bytesSent, totalBytesSent, totalBytesBuffered, CommandId.NO_COMMAND_ID);
    }

    public DataSent(
            final ConnectionId connectionId,
            final int bytesSent,
            final long totalBytesSent,
            final long totalBytesBuffered,
            final long commandId
    )
    {
        this(connectionId.port(), connectionId.connectionId(), bytesSent, totalBytesSent, totalBytesBuffered, commandId);
    }

    public DataSent(final int port, final long connectionId, final int bytesSent, final long totalBytesSent, final long totalBytesBuffered)
    {
        this(port, connectionId, bytesSent, totalBytesSent, totalBytesBuffered, CommandId.NO_COMMAND_ID);
    }

    public DataSent(final int port, final long connectionId, final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final long commandId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.bytesSent = bytesSent;
        this.totalBytesSent = totalBytesSent;
        this.commandId = commandId;
        this.totalBytesBuffered = totalBytesBuffered;
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

    public long totalBytesBuffered()
    {
        return totalBytesBuffered;
    }

    public int bytesSent()
    {
        return bytesSent;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public String toString()
    {
        return "DataSent{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", bytesSent=" + bytesSent +
               ", totalBytesSent=" + totalBytesSent +
               ", totalBytesBuffered=" + totalBytesBuffered +
               ", commandId=" + commandId +
               '}';
    }
}
