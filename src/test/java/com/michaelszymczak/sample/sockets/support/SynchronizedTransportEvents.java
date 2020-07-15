package com.michaelszymczak.sample.sockets.support;

import java.util.List;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;

public class SynchronizedTransportEvents implements TransportEventsListener
{
    private final TransportEventsSpy transportEvents = new TransportEventsSpy();

    @Override
    public synchronized void onEvent(final TransportEvent event)
    {
        transportEvents.onEvent(event);
    }

    public synchronized List<TransportEvent> events()
    {
        return transportEvents.all();
    }

    public synchronized <T extends TransportEvent> T last(final Class<T> clazz)
    {
        return transportEvents.last(clazz);
    }
}
