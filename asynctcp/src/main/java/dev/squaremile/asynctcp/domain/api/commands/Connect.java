package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class Connect implements TransportUserCommand
{
    private int remotePort = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private String remoteHost;
    private int timeoutMs = 1_000;
    private StandardEncoding encoding;

    public Connect set(final String remoteHost, final int remotePort, final long commandId, final int timeoutMs)
    {
        return set(remoteHost, remotePort, commandId, timeoutMs, StandardEncoding.RAW_STREAMING);
    }

    public Connect set(
            final String remoteHost,
            final int remotePort,
            final long commandId,
            final int timeoutMs,
            final StandardEncoding encoding
    )
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.commandId = commandId;
        this.timeoutMs = timeoutMs;
        this.encoding = encoding;
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

    public String encodingName()
    {
        return encoding.name();
    }

    @Override
    public String toString()
    {
        return "Connect{" +
               "remotePort=" + remotePort +
               ", commandId=" + commandId +
               ", remoteHost='" + remoteHost + '\'' +
               ", timeoutMs=" + timeoutMs +
               ", encodingName='" + encoding + '\'' +
               '}';
    }

    @Override
    public Connect copy()
    {
        return new Connect().set(remoteHost, remotePort, commandId, timeoutMs, encoding);
    }
}
