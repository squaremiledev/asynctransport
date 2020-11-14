package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class SingleConnectionTransport implements ConnectionTransport
{
    private final Transport transport;
    private final ConnectionId connectionId;

    public SingleConnectionTransport(final Transport transport, final ConnectionId connectionId)
    {
        this.transport = transport;
        this.connectionId = new ConnectionIdValue(connectionId);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final Class<C> commandType)
    {
        return transport.command(connectionId.connectionId(), commandType);
    }

    @Override
    public void handle(final ConnectionCommand command)
    {
        if (command.connectionId() != connectionId.connectionId())
        {
            throw new IllegalArgumentException("Connection id mismatch " + command.connectionId() + " vs " + connectionId.connectionId());
        }
        transport.handle(command);
    }
}
