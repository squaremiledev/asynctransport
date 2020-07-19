package com.michaelszymczak.sample.sockets.api.commands;

import java.nio.ByteBuffer;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionId;

public class SendData implements ConnectionCommand
{
    private final int port;
    private final long connectionId;
    private final ByteBuffer buffer;
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
        this.buffer = ByteBuffer.allocate(capacity);
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
        buffer.position(0).limit(length);
        return buffer;
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

    public SendData set(final byte[] content, final long commandId)
    {
        this.buffer.clear();
        this.buffer.put(content);
        this.length = content.length;
        this.commandId = commandId;
        return this;
    }
}
