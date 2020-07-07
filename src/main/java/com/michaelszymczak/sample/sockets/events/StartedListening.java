package com.michaelszymczak.sample.sockets.events;

public class StartedListening implements Event
{
    private final long commandId;
    private final long sessionId;

    public StartedListening(final long commandId, final long sessionId)
    {
        this.commandId = commandId;
        this.sessionId = sessionId;
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

    @Override
    public String toString()
    {
        return "StartedListening{" +
               "commandId=" + commandId +
               ", sessionId=" + sessionId +
               '}';
    }

}
