package dev.squaremile.asynctcp.domain.api;

import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;
import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;

public interface Transport extends AutoCloseable, TransportCommandHandler
{
    void work();

    @Override
    void close();

    <C extends TransportCommand> C command(Class<C> commandType);

    <C extends ConnectionCommand> C command(ConnectionId connectionId, Class<C> commandType);
}
