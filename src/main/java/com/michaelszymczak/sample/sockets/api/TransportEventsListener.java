package com.michaelszymczak.sample.sockets.api;

import com.michaelszymczak.sample.sockets.api.events.TransportEvent;

public interface TransportEventsListener
{
    void onEvent(TransportEvent event);
}
