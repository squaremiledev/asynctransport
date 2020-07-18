package com.michaelszymczak.sample.sockets.api.commands;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionId;

public class SendData implements ConnectionCommand
{
    private final int port;
    private final long connectionId;
    private byte[] content = null;
    private long commandId = CommandId.NO_COMMAND_ID;

    public SendData(final ConnectionId connectionId)
    {
        this(connectionId.port(), connectionId.connectionId());
    }

    public SendData(final int port, final long connectionId)
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
    public long commandId()
    {
        return commandId;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    public byte[] content()
    {
        return content;
    }

    public SendData set(final byte[] content)
    {
        this.content = content;
        return this;
    }

    public SendData set(final byte[] content, final long commandId)
    {
        this.content = content;
        this.commandId = commandId;
        return this;
    }

}
