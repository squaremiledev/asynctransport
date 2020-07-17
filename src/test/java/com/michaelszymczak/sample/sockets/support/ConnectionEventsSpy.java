package com.michaelszymczak.sample.sockets.support;

import java.util.List;
import java.util.function.Predicate;

import com.michaelszymczak.sample.sockets.api.events.ConnectionEvent;

public final class ConnectionEventsSpy
{
    private final TransportEventsSpy events;

    public ConnectionEventsSpy(final TransportEventsSpy events)
    {
        this.events = events;
    }

    public <T extends ConnectionEvent> boolean contains(final Class<T> itemType, final long connectionId)
    {
        return events.contains(itemType, event -> event.connectionId() == connectionId);
    }

    public <T extends ConnectionEvent> List<T> all(final Class<T> itemType, final long connectionId)
    {
        return events.all(itemType, event -> event.connectionId() == connectionId);
    }

    public <T extends ConnectionEvent> List<T> all(final Class<T> itemType, final long connectionId, final Predicate<T> predicate)
    {
        return events.all(itemType, event -> event.connectionId() == connectionId && predicate.test(event));
    }

    public <T extends ConnectionEvent> T last(final Class<T> clazz, long connectionId)
    {
        return events.last(clazz, event -> event.connectionId() == connectionId);
    }

    public <T extends ConnectionEvent> T last(final Class<T> clazz, long connectionId, final Predicate<T> predicate)
    {
        return events.last(clazz, event -> event.connectionId() == connectionId && predicate.test(event));
    }
}
