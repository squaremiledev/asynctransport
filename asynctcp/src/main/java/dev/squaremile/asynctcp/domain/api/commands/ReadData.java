package dev.squaremile.asynctcp.domain.api.commands;

import dev.squaremile.asynctcp.domain.api.ConnectionId;

public class ReadData implements ConnectionCommand
{
    private final int port;
    private final long connectionId;

    public ReadData(final ConnectionId connectionId)
    {
        this.port = connectionId.port();
        this.connectionId = connectionId.connectionId();
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long commandId()
    {
        return NO_COMMAND_ID;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    @Override
    public String toString()
    {
        return "ReadData{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               '}';
    }
}
