package com.michaelszymczak.sample.sockets.support;

import java.util.ArrayList;
import java.util.List;

import com.michaelszymczak.sample.sockets.events.TransportEvent;
import com.michaelszymczak.sample.sockets.events.TransportEventsListener;

public class TransportEvents implements TransportEventsListener
{
    private final List<TransportEvent> events = new ArrayList<>();

    @Override
    public void onEvent(final TransportEvent event)
    {
        events.add(event);
    }

    public List<TransportEvent> events()
    {
        return new ArrayList<>(events);
    }

    public <T> T last(final Class<T> clazz)
    {
        for (int k = events.size() - 1; k >= 0; k--)
        {
            if (clazz.isInstance(events.get(k)))
            {
                return clazz.cast(events.get(k));
            }
        }
        throw new IllegalStateException(clazz.getCanonicalName() + " not received");
    }
}
