package com.michaelszymczak.sample.sockets.api.commands;

import java.nio.ByteBuffer;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionId;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class SendData implements ConnectionCommand
{
    private final int port;
    private final long connectionId;
    private final int capacity;
    private final MutableDirectBuffer buffer;
    private int length;
    private long commandId = CommandId.NO_COMMAND_ID;

    public SendData(final ConnectionId connectionId, final int capacity)
    {
        this(connectionId.port(), connectionId.connectionId(), capacity);
    }

    public SendData(final int port, final long connectionId, final int capacity)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.capacity = capacity;
        this.buffer = new UnsafeBuffer(ByteBuffer.allocate(capacity));
        this.length = 0;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    public ByteBuffer byteBuffer()
    {
        buffer.byteBuffer().position(0).limit(length);
        return buffer.byteBuffer();
    }

    public SendData reset()
    {
        commandId = CommandId.NO_COMMAND_ID;
        length = 0;
        buffer.byteBuffer().position(0).limit(length);
        return this;
    }

    public SendData set(final byte[] content)
    {
        return set(content, CommandId.NO_COMMAND_ID);
    }

    public SendData set(final byte[] content, final long commandId)
    {
        this.length = Math.min(content.length, capacity);
        this.buffer.putBytes(0, content, 0, length);
        this.commandId = commandId;
        return this;
    }
}
