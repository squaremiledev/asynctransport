package dev.squaremile.asynctcp.domain.api.commands;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;

public class SendData implements ConnectionCommand
{
    private final ConnectionId connectionId;
    private final ByteBuffer buffer;
    private final int capacity;
    private int length;
    private long commandId;

    public SendData(final ConnectionId connectionId, final int capacity)
    {
        this(connectionId.port(), connectionId.connectionId(), capacity);
    }

    public SendData(final int port, final long connectionId, final int capacity)
    {
        this(new ConnectionIdValue(port, connectionId), ByteBuffer.allocate(capacity), capacity, 0, CommandId.NO_COMMAND_ID);
    }

    public SendData(final ConnectionId connectionId, final ByteBuffer buffer, final int capacity, final int length, final long commandId)
    {

        this.connectionId = connectionId;
        this.buffer = buffer;
        this.capacity = capacity;
        this.length = length;
        this.commandId = commandId;
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

    public ByteBuffer byteBuffer()
    {
        buffer.position(0).limit(length);
        return buffer;
    }

    public ByteBuffer prepareForWriting()
    {
        length = 0;
        buffer.position(0).limit(buffer.capacity());
        return buffer;
    }

    public SendData commitWriting(int length)
    {
        this.length = length;
        return this;
    }

    public SendData reset()
    {
        commandId = CommandId.NO_COMMAND_ID;
        length = 0;
        buffer.clear();
        return this;
    }

    public SendData set(final byte[] content)
    {
        return set(content, CommandId.NO_COMMAND_ID);
    }

    public SendData length(int length)
    {
        this.length = length;
        return this;
    }

    public SendData set(final byte[] content, final long commandId)
    {
        this.buffer.clear();
        this.buffer.put(content);
        this.length = content.length;
        this.commandId = commandId;
        return this;
    }

    @Override
    public String toString()
    {
        return "SendData{" +
               ", connectionId=" + connectionId +
               ", buffer=" + buffer +
               ", length=" + length +
               ", commandId=" + commandId +
               '}';
    }

    @Override
    public SendData copy()
    {
        return new SendData(connectionId, buffer, capacity, length, commandId);
    }
}
