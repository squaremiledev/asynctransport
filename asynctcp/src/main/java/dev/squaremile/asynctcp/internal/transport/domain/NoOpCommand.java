package dev.squaremile.asynctcp.internal.transport.domain;

import dev.squaremile.asynctcp.api.transport.app.ConnectionCommand;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;

public class NoOpCommand implements ConnectionCommand
{
    private final int port;
    private final long connectionId;

    public NoOpCommand(final ConnectionId connectionId)
    {
        this(connectionId.port(), connectionId.connectionId());
    }

    public NoOpCommand(final int port, final long connectionId)
    {
        this.port = port;
        this.connectionId = connectionId;
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
    public NoOpCommand copy()
    {
        return new NoOpCommand(port, connectionId);
    }
}
