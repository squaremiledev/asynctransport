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
}
