package dev.squaremile.asynctcp.domain.api.events;

import java.nio.ByteBuffer;
import java.util.Arrays;


import dev.squaremile.asynctcp.domain.api.ConnectionId;

public class MessageReceived implements ConnectionEvent
{
    private ConnectionId connectionId;
    private ByteBuffer data;
    private int length;

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

    public MessageReceived set(final ConnectionId dataReceived, final ByteBuffer data, final int length)
    {
        this.connectionId = dataReceived;
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
        return new MessageReceived().set(connectionId, ByteBuffer.wrap(Arrays.copyOf(data.array(), data.array().length)), length);
    }
}
