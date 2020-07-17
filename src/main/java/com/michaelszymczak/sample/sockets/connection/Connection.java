package com.michaelszymczak.sample.sockets.connection;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;

public interface Connection extends AutoCloseable, ConnectionId
{
    void handle(ConnectionCommand command);

    boolean isClosed();
}
