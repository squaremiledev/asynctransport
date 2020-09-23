package dev.squaremile.asynctcp.api.app;

public interface CommandFailed extends TransportCorrelatedEvent
{
    String details();
}
