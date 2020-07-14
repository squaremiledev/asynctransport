package com.michaelszymczak.sample.sockets.api.events;

public interface CommandFailed extends TransportEvent, CorrelatedEvent
{
    String details();
}
