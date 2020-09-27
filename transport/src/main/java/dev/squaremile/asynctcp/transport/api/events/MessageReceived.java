package dev.squaremile.asynctcp.transport.api.events;

import java.nio.ByteBuffer;
import java.util.Arrays;


import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class MessageReceived implements ConnectionEvent
{
    private ConnectionId connectionId;
    private ByteBuffer data;
    private int length;

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

    public MessageReceived set(final ByteBuffer data, final int length)
    {
        this.data = data;
        this.length = length;
        return this;
    }

    public MessageReceived set(final ConnectionId connectionId, final ByteBuffer data, final int length)
    {
        this.connectionId = connectionId;
        this.data = data;
        this.length = length;
        return this;
    }

    public void copyDataTo(final ByteBuffer dst)
    {
        data.clear().limit(length);
        dst.put(data);
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

    public ByteBuffer data()
    {
        data.position(0).limit(length);
        return data;
    }

    @Override
    public MessageReceived copy()
    {
        return new MessageReceived(connectionId).set(ByteBuffer.wrap(Arrays.copyOf(data.array(), data.array().length)), length);
    }
}
