package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class Listen implements TransportUserCommand
{
    private int port = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private StandardEncoding encoding;

    public Listen set(final long commandId, final int port)
    {
        return set(commandId, port, StandardEncoding.RAW_STREAMING);
    }

    public Listen set(final long commandId, final int port, final StandardEncoding encoding)
    {
        this.port = port;
        this.commandId = commandId;
        this.encoding = encoding;
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

    public String encodingName()
    {
        return encoding.name();
    }

    @Override
    public String toString()
    {
        return "Listen{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", encoding=" + encoding +
               '}';
    }

    @Override
    public Listen copy()
    {
        return new Listen().set(commandId, port, encoding);
    }
}
