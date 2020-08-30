package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class Connect implements TransportCommand
{
    private int remotePort = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private String remoteHost;

    public Connect set(final String remoteHost, final int remotePort, final long commandId)
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.commandId = commandId;
        return this;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public int port()
    {
        return NO_PORT;
    }

    public int remotePort()
    {
        return remotePort;
    }

    public String remoteHost()
    {
        return remoteHost;
    }
}
