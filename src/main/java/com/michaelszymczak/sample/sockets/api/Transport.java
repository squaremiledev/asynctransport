package com.michaelszymczak.sample.sockets.api;

import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;

public interface Transport
{
    void handle(TransportCommand command);
}
