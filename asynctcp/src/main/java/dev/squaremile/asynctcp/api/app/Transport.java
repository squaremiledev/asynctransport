package dev.squaremile.asynctcp.api.app;

import dev.squaremile.asynctcp.api.values.ConnectionId;

public interface Transport extends AutoCloseable, TransportCommandHandler
{
    void work();

    @Override
    void close();

    <C extends TransportUserCommand> C command(Class<C> commandType);

    <C extends ConnectionUserCommand> C command(ConnectionId connectionId, Class<C> commandType);
}
