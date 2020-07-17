package com.michaelszymczak.sample.sockets.api.commands;

public class ReadData implements ConnectionCommand
{
    private final int port;
    private final long connectionId;

    public ReadData(final int port, final long connectionId)
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
        return NO_COMMAND_ID;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }
}