package com.michaelszymczak.sample.sockets.domain.api;

import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.api.commands.TransportCommand;

public interface Transport extends AutoCloseable
{
    void work();

    void handle(TransportCommand command);

    @Override
    void close();

    <C extends TransportCommand> C command(Class<C> commandType);

    <C extends ConnectionCommand> C command(ConnectionId connectionId, Class<C> commandType);
}
