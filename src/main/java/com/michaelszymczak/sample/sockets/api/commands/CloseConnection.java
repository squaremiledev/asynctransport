package com.michaelszymczak.sample.sockets.api.commands;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.TransportId;

public class CloseConnection implements ConnectionCommand
{
    private int port = TransportId.NO_PORT;
    private long connectionId = ConnectionId.NO_CONNECTION;
    private long commandId = CommandId.NO_COMMAND_ID;

    public CloseConnection()
    {

    }

    public CloseConnection(final int port, final long connectionId, final long commandId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.commandId = commandId;
    }

    public CloseConnection set(final int port, final long connectionId, final long commandId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.commandId = commandId;
        return this;
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
}
