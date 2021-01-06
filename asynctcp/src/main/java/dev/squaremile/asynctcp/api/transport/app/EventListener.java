package dev.squaremile.asynctcp.api.transport.app;

public interface EventListener
{
    EventListener IGNORE_EVENTS = event ->
    {

    };

    void onEvent(Event event);
}
