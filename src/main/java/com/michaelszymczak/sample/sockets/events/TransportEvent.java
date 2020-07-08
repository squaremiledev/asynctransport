package com.michaelszymczak.sample.sockets.events;

public interface TransportEvent
{
    int port();

    long commandId();
}
