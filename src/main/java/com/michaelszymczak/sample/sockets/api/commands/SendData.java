package com.michaelszymczak.sample.sockets.api.commands;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.TransportId;
import com.michaelszymczak.sample.sockets.connection.Connection;

public class SendData implements ConnectionCommand
{
    private int port = TransportId.NO_PORT;
    private long connectionId = ConnectionId.NO_CONNECTION;
    private byte[] content = null;
    private long commandId = CommandId.NO_COMMAND_ID;

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

    public SendData set(final Connection connectionId)
    {
        this.port = connectionId.port();
        this.connectionId = connectionId.connectionId();
        this.content = null;
        this.commandId = CommandId.NO_COMMAND_ID;
        return this;
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

    public SendData set(final int port, final long connectionId, final byte[] content)
    {
        return set(port, connectionId, content, CommandId.NO_COMMAND_ID);
    }

    public SendData set(final int port, final long connectionId, final byte[] content, final long commandId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.content = content;
        this.commandId = commandId;
        return this;
    }
}
