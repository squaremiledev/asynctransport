package dev.squaremile.asynctcp.transport.api.events;

import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class DataSent implements ConnectionEvent, TransportCorrelatedEvent
{
    private int port;
    private long connectionId;
    private int sendBufferSize;
    private int bytesSent;
    private long totalBytesSent;
    private long totalBytesBuffered;
    private long commandId;
    private long windowSizeInBytes;

    public DataSent()
    {
        reset();
    }

    public DataSent(final ConnectionId connectionId, final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final int sendBufferSize)
    {
        this(connectionId, bytesSent, totalBytesSent, totalBytesBuffered, CommandId.NO_COMMAND_ID, sendBufferSize);
    }

    public DataSent(
            final ConnectionId connectionId,
            final int bytesSent,
            final long totalBytesSent,
            final long totalBytesBuffered,
            final long commandId,
            final int sendBufferSize
    )
    {
        this(connectionId.port(), connectionId.connectionId(), bytesSent, totalBytesSent, totalBytesBuffered, commandId, sendBufferSize);
    }

    public DataSent(final int port, final long connectionId, final int sendBufferSize)
    {
        this(port, connectionId, -1, -1, -1, sendBufferSize);
    }

    public DataSent(final int port, final long connectionId, final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final int sendBufferSize)
    {
        this(port, connectionId, bytesSent, totalBytesSent, totalBytesBuffered, CommandId.NO_COMMAND_ID, sendBufferSize);
    }

    public DataSent(final int port, final long connectionId, final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final long commandId, final int sendBufferSize)
    {
        set(port, connectionId, bytesSent, totalBytesSent, totalBytesBuffered, commandId, sendBufferSize);
    }

    public DataSent reset()
    {
        port = -1;
        connectionId = -1;
        sendBufferSize = 0;
        bytesSent = 0;
        totalBytesSent = 0;
        totalBytesBuffered = 0;
        commandId = CommandId.NO_COMMAND_ID;
        windowSizeInBytes = 0;
        return this;
    }

    public DataSent set(final int port, final long connectionId, final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final long commandId, final int sendBufferSize)
    {
        reset();
        this.port = port;
        this.connectionId = connectionId;
        this.bytesSent = bytesSent;
        this.totalBytesSent = totalBytesSent;
        this.commandId = commandId;
        this.totalBytesBuffered = totalBytesBuffered;
        this.sendBufferSize = sendBufferSize;
        this.windowSizeInBytes = sendBufferSize + totalBytesSent - totalBytesBuffered;
        return this;
    }

    public DataSent set(final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final long commandId)
    {
        this.bytesSent = bytesSent;
        this.totalBytesSent = totalBytesSent;
        this.commandId = commandId;
        this.totalBytesBuffered = totalBytesBuffered;
        this.windowSizeInBytes = sendBufferSize + totalBytesSent - totalBytesBuffered;
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

    public int sendBufferSize()
    {
        return sendBufferSize;
    }

    public long totalBytesBuffered()
    {
        return totalBytesBuffered;
    }

    public long windowSizeInBytes()
    {
        return windowSizeInBytes;
    }

    public long originalWindowSizeInBytes()
    {
        return sendBufferSize;
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
               ", sendBufferSize=" + sendBufferSize +
               ", bytesSent=" + bytesSent +
               ", totalBytesSent=" + totalBytesSent +
               ", totalBytesBuffered=" + totalBytesBuffered +
               ", commandId=" + commandId +
               ", originalWindowSizeInBytes=" + sendBufferSize +
               ", windowSizeInBytes=" + windowSizeInBytes +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new DataSent(port, connectionId, bytesSent, totalBytesSent, totalBytesBuffered, commandId, sendBufferSize);
    }
}
