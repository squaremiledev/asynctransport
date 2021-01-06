package dev.squaremile.asynctcp.api.transport.app;

public interface TransportApplicationFactory
{
    TransportApplication create(Transport transport);
}
