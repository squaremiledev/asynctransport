package com.michaelszymczak.sample.sockets.api;

public interface ConnectionId extends TransportId
{
    int NO_CONNECTION = -1;

    long connectionId();
}
