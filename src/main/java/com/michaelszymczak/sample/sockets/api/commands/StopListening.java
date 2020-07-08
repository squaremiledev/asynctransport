package com.michaelszymczak.sample.sockets.api.commands;

import com.michaelszymczak.sample.sockets.api.TransportCommand;

public class StopListening implements TransportCommand
{
    private final int commandId;
    private final int port;

    public StopListening(final int commandId, final int port)
    {
        this.commandId = commandId;
        this.port = port;
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
