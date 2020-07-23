package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;
import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;

public interface Connection extends ConnectionId, AutoCloseable
{
    boolean handle(ConnectionCommand command);

    <C extends ConnectionCommand> C command(Class<C> commandType);

    ConnectionState state();
}
