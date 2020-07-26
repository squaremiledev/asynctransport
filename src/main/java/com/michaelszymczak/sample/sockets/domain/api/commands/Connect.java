package com.michaelszymczak.sample.sockets.domain.api.commands;

import com.michaelszymczak.sample.sockets.domain.api.CommandId;
import com.michaelszymczak.sample.sockets.domain.api.TransportId;

public class Connect implements TransportCommand
{
    private int port = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;

    public Connect set(final int port, final long commandId)
    {
        this.port = port;
        this.commandId = commandId;
        return this;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public int port()
    {
        return port;
    }
}
