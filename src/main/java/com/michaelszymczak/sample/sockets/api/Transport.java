package com.michaelszymczak.sample.sockets.api;

import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.nio.Workmen;

public interface Transport extends AutoCloseable, Workmen.NonBlockingWorkman
{
    void handle(TransportCommand command);
}
