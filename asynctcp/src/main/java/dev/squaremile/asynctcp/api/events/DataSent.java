package dev.squaremile.asynctcp.api.events;

import dev.squaremile.asynctcp.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.api.app.TransportEvent;
import dev.squaremile.asynctcp.api.values.CommandId;
import dev.squaremile.asynctcp.api.values.ConnectionId;

public class DataSent implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long connectionId;
    private int bytesSent;
    private long totalBytesSent;
    private long totalBytesBuffered;
    private long commandId;

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

    public DataSent(final int port, final long connectionId)
    {
        this(port, connectionId, -1, -1, -1, CommandId.NO_COMMAND_ID);
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

    public DataSent set(final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final long commandId)
    {
        this.bytesSent = bytesSent;
        this.totalBytesSent = totalBytesSent;
        this.commandId = commandId;
        this.totalBytesBuffered = totalBytesBuffered;
        return this;
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

    @Override
    public TransportEvent copy()
    {
        return new DataSent(port, connectionId, bytesSent, totalBytesSent, totalBytesBuffered, commandId);
    }
}
