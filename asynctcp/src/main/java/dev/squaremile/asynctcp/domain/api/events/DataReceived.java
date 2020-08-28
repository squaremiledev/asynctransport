package dev.squaremile.asynctcp.domain.api.events;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataReceived implements ConnectionEvent
{
    private final int port;
    private final long connectionId;
    private final ByteBuffer data;
    private long totalBytesReceived;
    private int length;
    private int inboundPduLimit;

    public DataReceived(final int port, final long connectionId, final int inboundPduLimit)
    {
        this(port, connectionId, 0, 0, inboundPduLimit, ByteBuffer.wrap(new byte[inboundPduLimit]));
    }

    private DataReceived(final int port, final long connectionId, final long totalBytesReceived, final int length, final int inboundPduLimit, final ByteBuffer data)
    {
        this.port = port;
        this.connectionId = connectionId;

        this.totalBytesReceived = totalBytesReceived;
        this.inboundPduLimit = inboundPduLimit;
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

    public void copyDataTo(final ByteBuffer dst)
    {
        data.clear().limit(length);
        dst.put(data);
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
        return new DataReceived(port, connectionId, totalBytesReceived, length, inboundPduLimit, ByteBuffer.wrap(Arrays.copyOf(data.array(), data.array().length)));
    }
}
