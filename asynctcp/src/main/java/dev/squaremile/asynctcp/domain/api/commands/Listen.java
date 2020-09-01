package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.StandardProtocol;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class Listen implements TransportCommand
{
    private int port = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private StandardProtocol protocol;

    public Listen set(final long commandId, final int port)
    {
        return set(commandId, port, StandardProtocol.RAW_STREAMING);
    }

    public Listen set(final long commandId, final int port, final StandardProtocol protocol)
    {
        this.port = port;
        this.commandId = commandId;
        this.protocol = protocol;
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

    public String protocolName()
    {
        return protocol.name();
    }

    @Override
    public String toString()
    {
        return "Listen{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", protocol=" + protocol +
               '}';
    }

    @Override
    public Listen copy()
    {
        return new Listen().set(commandId, port, protocol);
    }
}
