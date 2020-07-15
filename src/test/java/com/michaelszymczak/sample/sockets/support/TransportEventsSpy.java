package com.michaelszymczak.sample.sockets.support;

import java.util.List;
import java.util.function.Predicate;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.events.TransportCorrelatedEvent;
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;

public class TransportEventsSpy implements TransportEventsListener
{
    private final CapturedItems<TransportEvent> items = new CapturedItems<>();

    public List<TransportEvent> all()
    {
        return items.all();
    }

    public <T extends TransportEvent> List<T> all(final Class<T> itemType)
    {
        return items.all(itemType, foundItem -> true);
    }

    public <T extends TransportEvent> List<T> all(final Class<T> itemType, final Predicate<T> predicate)
    {
        return items.all(itemType, predicate);
    }

    public <T extends TransportEvent> T last(final Class<T> clazz)
    {
        return items.last(clazz, event -> true);
    }

    public <T extends TransportEvent> T last(final Class<T> clazz, final Predicate<T> predicate)
    {
        return items.last(clazz, predicate);
    }

    public <T extends TransportCorrelatedEvent> T lastResponse(final Class<T> eventType, final int commandId)
    {
        return items.last(eventType, event -> event.commandId() == commandId);
    }

    @Override
    public void onEvent(final TransportEvent event)
    {
        items.add(event);
    }

    @Override
    public String toString()
    {
        return items.toString();
    }
}
