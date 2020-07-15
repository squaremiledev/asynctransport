package com.michaelszymczak.sample.sockets.api;

import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;

public interface Transport extends AutoCloseable
{
    void work();

    void handle(TransportCommand command);

    @Override
    void close();
}
