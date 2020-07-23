package com.michaelszymczak.sample.sockets.domain.api.events;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataReceived implements ConnectionEvent
{
    private final int port;
    private final long connectionId;
    private final ByteBuffer data;
    private long totalBytesReceived;
    private int length;
    private int maxInboundMessageSize;

    public DataReceived(final int port, final long connectionId, final int maxInboundMessageSize)
    {
        this(port, connectionId, 0, 0, maxInboundMessageSize, ByteBuffer.wrap(new byte[maxInboundMessageSize]));
    }

    private DataReceived(final int port, final long connectionId, final long totalBytesReceived, final int length, final int maxInboundMessageSize, final ByteBuffer data)
    {
        this.port = port;
        this.connectionId = connectionId;

        this.totalBytesReceived = totalBytesReceived;
        this.maxInboundMessageSize = maxInboundMessageSize;
        this.data = data;
        this.length = length;
    }

    public ByteBuffer prepare()
    {
        length = -1;
        totalBytesReceived = -1;
        data.clear();
        return data;
    }

    public DataReceived commit(final int length, final long totalBytesReceived)
    {
        this.totalBytesReceived = totalBytesReceived;
        this.length = length;
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

    public void copyDataTo(byte[] dst)
    {
        System.arraycopy(data.array(), 0, dst, 0, length);
    }

    public int length()
    {
        return length;
    }

    public long totalBytesReceived()
    {
        return totalBytesReceived;
    }

    @Override
    public String toString()
    {
        return "DataReceived{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", totalBytesReceived=" + totalBytesReceived +
               ", length=" + length +
               ", data=" + data +
               '}';
    }

    @Override
    public DataReceived copy()
    {
        return new DataReceived(port, connectionId, totalBytesReceived, length, maxInboundMessageSize, ByteBuffer.wrap(Arrays.copyOf(data.array(), data.array().length)));
    }
}
