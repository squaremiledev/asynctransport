package dev.squaremile.asynctcp.transport.api.app;

public interface ApplicationFactory
{
    EventDrivenApplication create(Transport transport);
}
