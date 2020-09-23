package dev.squaremile.asynctcp.internal.domain.connection;

import dev.squaremile.asynctcp.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.values.ConnectionId;

public interface Connection extends ConnectionId, AutoCloseable
{
    boolean handle(ConnectionCommand command);

    <C extends ConnectionUserCommand> C command(Class<C> commandType);

    ConnectionState state();

    void accepted(long commandIdThatTriggeredListening);

    void connected(long commandId);
}
