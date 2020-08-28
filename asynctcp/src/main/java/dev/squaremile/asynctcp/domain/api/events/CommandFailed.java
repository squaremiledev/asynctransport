package dev.squaremile.asynctcp.domain.api.events;

public interface CommandFailed extends TransportCorrelatedEvent
{
    String details();
}
