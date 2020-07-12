package com.michaelszymczak.sample.sockets.api.commands;

public class SendData implements ConnectionCommand
{
    private final int port;
    private final long connectionId;
    private final byte[] content;
    private final long commandId;

    public SendData(final int port, final long connectionId, final byte[] content)
    {
        this(port, connectionId, content, CONVENTIONAL_IGNORED_COMMAND_ID);
    }

    public SendData(final int port, final long connectionId, final byte[] content, final long commandId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.content = content;
        this.commandId = commandId;
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
