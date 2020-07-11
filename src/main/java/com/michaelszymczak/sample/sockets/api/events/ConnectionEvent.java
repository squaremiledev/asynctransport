package com.michaelszymczak.sample.sockets.api.events;

public interface ConnectionEvent extends TransportEvent
{
    long connectionId();
}
