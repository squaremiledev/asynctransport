package com.michaelszymczak.sample.sockets.api.events;

public interface CommandFailed extends TransportCorrelatedEvent
{
    String details();
}
