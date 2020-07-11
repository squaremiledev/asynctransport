package com.michaelszymczak.sample.sockets.connection;

import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;

public interface ConnectionAggregate extends AutoCloseable
{
    int port();

    long connectionId();

    void handle(ConnectionCommand command);
}
