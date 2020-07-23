package com.michaelszymczak.sample.sockets.domain.api.events;

public interface TransportEventsListener
{
    void onEvent(TransportEvent event);
}
