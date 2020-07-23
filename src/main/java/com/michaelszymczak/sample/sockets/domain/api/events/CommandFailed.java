package com.michaelszymczak.sample.sockets.domain.api.events;

public interface CommandFailed extends TransportCorrelatedEvent
{
    String details();
}
