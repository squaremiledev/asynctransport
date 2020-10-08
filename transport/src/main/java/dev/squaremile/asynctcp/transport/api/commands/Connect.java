package dev.squaremile.asynctcp.transport.api.commands;

import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcp.transport.api.values.TransportId;

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
