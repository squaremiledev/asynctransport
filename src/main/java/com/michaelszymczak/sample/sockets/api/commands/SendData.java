package com.michaelszymczak.sample.sockets.api.commands;

public class SendData implements ConnectionCommand
{
    private final int port;
    private final long connectionId;
    private final byte[] content;

    public SendData(final int port, final long connectionId, final byte[] content)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.content = content;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    public byte[] content()
    {
        return content;
    }
}
