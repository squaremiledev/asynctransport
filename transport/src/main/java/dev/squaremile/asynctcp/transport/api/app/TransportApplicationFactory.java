package dev.squaremile.asynctcp.transport.api.app;

public interface TransportApplicationFactory
{
    TransportApplication create(Transport transport);
}
