package com.michaelszymczak.sample.sockets.commands;

public class StopListening implements Command
{
    private final int commandId;
    private final long sessionId;

    public StopListening(final int commandId, final long sessionId)
    {
        this.commandId = commandId;
        this.sessionId = sessionId;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    public long sessionId()
    {
        return sessionId;
    }
}
