package dev.squaremile.asynctcp.domain.api.events;

public interface EventListener
{
    EventListener IGNORE_EVENTS = event ->
    {

    };

    void onEvent(Event event);
}
