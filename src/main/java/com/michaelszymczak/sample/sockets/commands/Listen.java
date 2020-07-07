package com.michaelszymczak.sample.sockets.commands;

public class Listen implements Command
{
    private final int port;
    private final int commandId;

    public Listen(final int commandId, final int port)
    {
        this.port = port;
        this.commandId = commandId;
    }

    public int port()
    {
        return port;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }
}
