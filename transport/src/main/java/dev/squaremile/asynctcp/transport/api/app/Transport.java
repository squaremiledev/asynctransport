package dev.squaremile.asynctcp.transport.api.app;

import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface Transport extends AutoCloseable, TransportCommandHandler, OnDuty
{
    @Override
    void close();

    <C extends TransportUserCommand> C command(Class<C> commandType);

    <C extends ConnectionUserCommand> C command(ConnectionId connectionId, Class<C> commandType);
}
