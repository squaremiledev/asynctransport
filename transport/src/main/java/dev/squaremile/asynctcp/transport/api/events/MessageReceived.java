package dev.squaremile.asynctcp.transport.api.events;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class MessageReceived implements ConnectionEvent
{
    private ConnectionId connectionId;
    private DirectBuffer data;
    private int length;
    private int offset;

    public MessageReceived()
    {

    }

    public MessageReceived(final ConnectionId connectionId)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
    }

    @Override
    public int port()
    {
        return connectionId.port();
    }

    @Override
    public long connectionId()
    {
        return connectionId.connectionId();
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
        return set(connectionId, data, offset, length);
    }

    public MessageReceived set(final ConnectionId connectionId, final DirectBuffer data, final int offset, final int length)
    {
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
               "connectionId=" + connectionId +
               ", data=" + data +
               ", length=" + length +
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
        return new MessageReceived(connectionId).set(bufferCopy, 0, length);
    }
}
