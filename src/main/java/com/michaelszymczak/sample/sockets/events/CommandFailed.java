package com.michaelszymczak.sample.sockets.events;

public class CommandFailed implements Event
{
    private final long commandId;
    private final long sessionId;
    private final String details;

    public CommandFailed(final long commandId, final long sessionId, final String details)
    {
        this.commandId = commandId;
        this.sessionId = sessionId;
        this.details = details;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public long sessionId()
    {
        return sessionId;
    }
}
