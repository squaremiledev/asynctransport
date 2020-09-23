package dev.squaremile.asynctcp.api.app;

public interface EventListener
{
    EventListener IGNORE_EVENTS = event ->
    {

    };

    void onEvent(Event event);
}
