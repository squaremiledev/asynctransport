package dev.squaremile.asynctcp.transport.api.commands;

import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportEncoding;
import dev.squaremile.asynctcp.transport.api.values.TransportId;

public class Listen implements TransportUserCommand
{
    private int port = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private PredefinedTransportEncoding encoding;

    public Listen set(final long commandId, final int port)
    {
        return set(commandId, port, PredefinedTransportEncoding.RAW_STREAMING);
    }

    public Listen set(final long commandId, final int port, final PredefinedTransportEncoding encoding)
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
