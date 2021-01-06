package dev.squaremile.asynctcp.api.transport.commands;

import dev.squaremile.asynctcp.api.transport.app.TransportUserCommand;
import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.api.transport.values.TransportId;

public class Connect implements TransportUserCommand
{
    private int remotePort = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private String remoteHost;
    private int timeoutMs = 1_000;
    private Delineation delineation;

    public Connect set(
            final String remoteHost,
            final int remotePort,
            final long commandId,
            final int timeoutMs,
            final Delineation delineation
    )
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.commandId = commandId;
        this.timeoutMs = timeoutMs;
        this.delineation = delineation;
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

    public int timeoutMs()
    {
        return timeoutMs;
    }

    public Delineation delineation()
    {
        return delineation;
    }

    @Override
    public String toString()
    {
        return "Connect{" +
               "remotePort=" + remotePort +
               ", commandId=" + commandId +
               ", remoteHost='" + remoteHost + '\'' +
               ", timeoutMs=" + timeoutMs +
               ", delineation='" + delineation + '\'' +
               '}';
    }

    @Override
    public Connect copy()
    {
        return new Connect().set(remoteHost, remotePort, commandId, timeoutMs, delineation);
    }
}
