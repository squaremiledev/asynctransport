package com.michaelszymczak.sample.sockets.connection;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;

public interface Connection extends AutoCloseable, ConnectionId
{
    boolean handle(ConnectionCommand command);

    boolean isClosed();

    <C extends ConnectionCommand> C command(Class<C> commandType);

    ConnectionState state();
}
