package dev.squaremile.asynctcp.transport.api.app;

public interface CommandFailed extends TransportCorrelatedEvent
{
    String details();
}
