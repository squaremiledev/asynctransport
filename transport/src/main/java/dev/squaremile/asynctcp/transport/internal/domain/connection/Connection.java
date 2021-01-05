package dev.squaremile.asynctcp.transport.internal.domain.connection;

import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.OnDuty;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface Connection extends ConnectionId, AutoCloseable, OnDuty
{
    boolean handle(ConnectionCommand command);

    <C extends ConnectionUserCommand> C command(Class<C> commandType);

    ConnectionState state();

    void accepted(long commandIdThatTriggeredListening);

    void connected(long commandId);
}
