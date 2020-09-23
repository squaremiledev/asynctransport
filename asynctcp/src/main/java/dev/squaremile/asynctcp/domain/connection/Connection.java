package dev.squaremile.asynctcp.domain.connection;

import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.commands.ConnectionUserCommand;
import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;

public interface Connection extends ConnectionId, AutoCloseable
{
    boolean handle(ConnectionCommand command);

    <C extends ConnectionUserCommand> C command(Class<C> commandType);

    ConnectionState state();

    void accepted(long commandIdThatTriggeredListening);

    void connected(long commandId);
}
