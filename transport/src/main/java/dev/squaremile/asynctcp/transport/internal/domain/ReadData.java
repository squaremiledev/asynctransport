package dev.squaremile.asynctcp.transport.internal.domain;

import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class ReadData implements ConnectionCommand
{
    private final ConnectionId connectionId;

    public ReadData(final ConnectionId connectionId)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
    }

    @Override
    public int port()
    {
        return connectionId.port();
    }

    @Override
    public long commandId()
    {
        return NO_COMMAND_ID;
    }

    @Override
    public long connectionId()
    {
        return connectionId.connectionId();
    }

    @Override
    public String toString()
    {
        return "ReadData{" +
               ", connectionId=" + connectionId +
               '}';
    }

    @Override
    public ReadData copy()
    {
        return new ReadData(connectionId);
    }
}
