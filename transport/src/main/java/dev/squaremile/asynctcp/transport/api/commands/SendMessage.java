package dev.squaremile.asynctcp.transport.api.commands;

import java.nio.ByteBuffer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class SendMessage implements ConnectionUserCommand
{
    private final ConnectionId connectionId;
    private final ByteBuffer data;
    private final MutableDirectBuffer buffer;
    private final int offset;
    private final int capacity;
    private int length;
    private long commandId;

    public SendMessage(final ConnectionId connectionId, final int capacity)
    {
        this(connectionId.port(), connectionId.connectionId(), capacity);
    }

    public SendMessage(final int port, final long connectionId, final int capacity)
    {
        this.capacity = capacity;
        this.connectionId = new ConnectionIdValue(port, connectionId);
        this.data = ByteBuffer.allocate(capacity);
        this.buffer = new UnsafeBuffer(data);
        this.length = 0;
        this.commandId = CommandId.NO_COMMAND_ID;
        this.offset = 0;
    }

    @Override
    public int port()
    {
        return connectionId.port();
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public long connectionId()
    {
        return connectionId.connectionId();
    }

    public MutableDirectBuffer prepare()
    {
        length = 0;
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public SendMessage reset()
    {
        commandId = CommandId.NO_COMMAND_ID;
        length = 0;
        return this;
    }

    public DirectBuffer buffer()
    {
        return buffer;
    }

    public int length()
    {
        return length;
    }

    @Override
    public String toString()
    {
        return "SendMessage{" +
               "connectionId=" + connectionId +
               ", buffer=" + buffer +
               ", offset=" + offset +
               ", length=" + length +
               ", commandId=" + commandId +
               '}';
    }

    @Override
    public SendMessage copy()
    {
        SendMessage copy = new SendMessage(connectionId, capacity);
        buffer.getBytes(offset, copy.prepare(), copy.offset(), length);
        copy.commit(length);
        return copy;
    }

    public SendMessage commit(final int length)
    {
        this.length = length;
        return this;
    }

    public ByteBuffer data()
    {
        data.position(0).limit(length);
        return data;
    }
}
