package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.api.events.TransportCorrelatedEvent;

public class TransportEventsSpy
{
    private final TransportEvents transportEvents;

    public TransportEventsSpy(final TransportEvents transportEvents)
    {
        this.transportEvents = transportEvents;
    }


    public <T extends TransportCorrelatedEvent> T lastResponse(final Class<T> eventType, final int commandId)
    {
        return transportEvents.last(eventType, event -> event.commandId() == commandId);
    }

    public boolean contains(final Class<? extends TransportCorrelatedEvent> eventType, final int commandId)
    {
        return !transportEvents.all(eventType, event -> event.commandId() == commandId).isEmpty();
    }
}
