package com.michaelszymczak.sample.sockets.api.commands;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.TransportId;

public class Listen implements TransportCommand
{
    private int port;
    private long commandId;

    public Listen()
    {
        port = TransportId.NO_PORT;
        commandId = CommandId.NO_COMMAND_ID;
    }

    public Listen(final int commandId, final int port)
    {
        set(commandId, port);
    }

    public Listen set(final int commandId, final int port)
    {
        this.port = port;
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
}
