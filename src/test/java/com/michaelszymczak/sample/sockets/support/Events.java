package com.michaelszymczak.sample.sockets.support;

import java.util.ArrayList;
import java.util.List;

import com.michaelszymczak.sample.sockets.events.Event;
import com.michaelszymczak.sample.sockets.events.EventsListener;

public class Events implements EventsListener
{
    private final List<Event> events = new ArrayList<>();

    @Override
    public void onEvent(final Event event)
    {
        events.add(event);
    }

    public List<Event> events()
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
        return null;
    }
}
