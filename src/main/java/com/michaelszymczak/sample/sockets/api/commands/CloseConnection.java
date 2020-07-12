package com.michaelszymczak.sample.sockets.api.commands;

public class CloseConnection implements ConnectionCommand
{
    private final int port;
    private final long connectionId;
    private final long commandId;

    public CloseConnection(final int port, final long connectionId, final long commandId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.commandId = commandId;
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
