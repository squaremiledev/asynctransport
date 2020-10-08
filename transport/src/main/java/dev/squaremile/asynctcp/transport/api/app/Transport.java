package dev.squaremile.asynctcp.transport.api.app;

import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface Transport extends TransportCommandHandler
{
    <C extends TransportUserCommand> C command(Class<C> commandType);

    <C extends ConnectionUserCommand> C command(ConnectionId connectionId, Class<C> commandType);
}
