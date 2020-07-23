package com.michaelszymczak.sample.sockets.domain.api.commands;

import com.michaelszymczak.sample.sockets.domain.api.CommandId;
import com.michaelszymczak.sample.sockets.domain.api.TransportId;

public class StopListening implements TransportCommand
{
    private long commandId = CommandId.NO_COMMAND_ID;
    private int port = TransportId.NO_PORT;

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

    public StopListening set(final long commandId, final int port)
    {
        this.commandId = commandId;
        this.port = port;
        return this;
    }
}
