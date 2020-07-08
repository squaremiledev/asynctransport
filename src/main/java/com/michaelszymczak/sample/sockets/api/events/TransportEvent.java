package com.michaelszymczak.sample.sockets.api.events;

public interface TransportEvent
{
    int port();

    long commandId();
}
