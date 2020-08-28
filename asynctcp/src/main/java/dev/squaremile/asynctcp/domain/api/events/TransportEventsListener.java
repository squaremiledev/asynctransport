package dev.squaremile.asynctcp.domain.api.events;

public interface TransportEventsListener
{
    void onEvent(TransportEvent event);
}
