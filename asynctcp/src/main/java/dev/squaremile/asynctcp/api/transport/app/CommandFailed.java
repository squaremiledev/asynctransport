package dev.squaremile.asynctcp.api.transport.app;

public interface CommandFailed extends TransportCorrelatedEvent
{
    String details();
}
