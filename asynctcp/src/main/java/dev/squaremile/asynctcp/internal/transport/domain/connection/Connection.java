package dev.squaremile.asynctcp.internal.transport.domain.connection;

import dev.squaremile.asynctcp.api.transport.app.ApplicationLifecycle;
import dev.squaremile.asynctcp.api.transport.app.ConnectionCommand;
import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;

public interface Connection extends ConnectionId, AutoCloseable, ApplicationLifecycle, OnDuty
{
    boolean handle(ConnectionCommand command);

    <C extends ConnectionUserCommand> C command(Class<C> commandType);

    ConnectionState state();

    void accepted(long commandIdThatTriggeredListening);

    void connected(long commandId);
}
