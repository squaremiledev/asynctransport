package com.michaelszymczak.sample.sockets.events;

public interface TransportEvent
{
    long commandId();

    int port();
}
