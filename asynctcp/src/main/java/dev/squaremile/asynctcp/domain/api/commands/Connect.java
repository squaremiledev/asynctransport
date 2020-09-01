package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.CommandId;
import dev.squaremile.asynctcp.domain.api.StandardProtocol;
import dev.squaremile.asynctcp.domain.api.TransportId;

public class Connect implements TransportCommand
{
    private int remotePort = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private String remoteHost;
    private int timeoutMs = 1_000;
    private StandardProtocol protocol;

    public Connect set(final String remoteHost, final int remotePort, final long commandId, final int timeoutMs)
    {
        return set(remoteHost, remotePort, commandId, timeoutMs, StandardProtocol.RAW_STREAMING);
    }

    public Connect set(
            final String remoteHost,
            final int remotePort,
            final long commandId,
            final int timeoutMs,
            final StandardProtocol protocol
    )
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.commandId = commandId;
        this.timeoutMs = timeoutMs;
        this.protocol = protocol;
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

    public String protocolName()
    {
        return protocol.name();
    }

    @Override
    public String toString()
    {
        return "Connect{" +
               "remotePort=" + remotePort +
               ", commandId=" + commandId +
               ", remoteHost='" + remoteHost + '\'' +
               ", timeoutMs=" + timeoutMs +
               ", protocolName='" + protocol + '\'' +
               '}';
    }

    @Override
    public Connect copy()
    {
        return new Connect().set(remoteHost, remotePort, commandId, timeoutMs, protocol);
    }
}
