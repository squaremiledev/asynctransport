package com.michaelszymczak.sample.sockets.api.commands;

import com.michaelszymczak.sample.sockets.api.TransportCommand;

public class Listen implements TransportCommand
{
    private final int port;
    private final int commandId;

    public Listen(final int commandId, final int port)
    {
        this.port = port;
        this.commandId = commandId;
    }

    @Override
    public int port()
    {
        return port;
    }

    public long commandId()
    {
        return commandId;
    }
}
