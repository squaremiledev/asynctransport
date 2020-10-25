package dev.squaremile.asynctcp.transport.api.events;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class MessageReceived implements ConnectionEvent
{
    private int port = -1;
    private long connectionId = -1;
    private DirectBuffer data = null;
    private int length = 0;
    private int offset = 0;

    public MessageReceived()
    {
    }

    public MessageReceived(final ConnectionId connectionId)
    {
        this(connectionId.port(), connectionId.connectionId());
    }

    public MessageReceived(final int port, final long connectionId)
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
    public long connectionId()
    {
        return connectionId;
    }

    public DirectBuffer buffer()
    {
        return data;
    }

    public int offset()
    {
        return offset;
    }

    public MessageReceived set(final DirectBuffer data, final int length)
    {
        return set(data, 0, length);
    }

    public MessageReceived set(final DirectBuffer data, final int offset, final int length)
    {
        return set(port, connectionId, data, offset, length);
    }

    public MessageReceived set(final int port, final long connectionId, final DirectBuffer data, final int offset, final int length)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.data = data;
        this.offset = offset;
        this.length = length;
        return this;
    }

    @Override
    public String toString()
    {
        return "MessageReceived{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", data=" + data +
               ", length=" + length +
               ", offset=" + offset +
               '}';
    }

    public int length()
    {
        return length;
    }


    @Override
    public MessageReceived copy()
    {
        UnsafeBuffer bufferCopy = new UnsafeBuffer(new byte[length]);
        data.getBytes(offset, bufferCopy, 0, length);
        return new MessageReceived(port, connectionId).set(bufferCopy, 0, length);
    }
}
