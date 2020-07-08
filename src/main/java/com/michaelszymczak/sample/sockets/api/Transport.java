package com.michaelszymczak.sample.sockets.api;

public interface Transport
{
    void handle(TransportCommand command);
}
