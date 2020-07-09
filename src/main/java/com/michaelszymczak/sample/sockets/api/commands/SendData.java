package com.michaelszymczak.sample.sockets.api.commands;

public class SendData implements TransportCommand
{
    private final int port;
    private final long connectionId;

    public SendData(final int port, final long connectionId)
    {
        this.port = port;
        this.connectionId = connectionId;
    }

    @Override
    public int port()
    {
        return port();
    }

    public long connectionId()
    {
        return connectionId;
    }
}
