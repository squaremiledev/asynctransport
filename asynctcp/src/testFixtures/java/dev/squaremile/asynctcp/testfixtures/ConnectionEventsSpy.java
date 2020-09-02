package dev.squaremile.asynctcp.testfixtures;

import java.util.List;
import java.util.function.Predicate;


import dev.squaremile.asynctcp.domain.api.events.ConnectionEvent;
import dev.squaremile.asynctcp.domain.connection.ConnectionEventsListener;

public final class ConnectionEventsSpy implements ConnectionEventsListener
{
    private final TransportEventsSpy events;

    public ConnectionEventsSpy()
    {
        this(new TransportEventsSpy());
    }

    public ConnectionEventsSpy(final TransportEventsSpy events)
    {
        this.events = events;
    }

    public <T extends ConnectionEvent> boolean contains(final Class<T> itemType, final long connectionId)
    {
        return events.contains(itemType, event -> event.connectionId() == connectionId);
    }

    public <T extends ConnectionEvent> List<T> all(final Class<T> itemType)
    {
        return events.all(itemType, event -> true);
    }

    public <T extends ConnectionEvent> List<T> all(final Class<T> itemType, final long connectionId)
    {
        return events.all(itemType, event -> event.connectionId() == connectionId);
    }

    public <T extends ConnectionEvent> List<T> all(final Class<T> itemType, final long connectionId, final Predicate<T> predicate)
    {
        return events.all(itemType, event -> event.connectionId() == connectionId && predicate.test(event));
    }

    public <T extends ConnectionEvent> T last(final Class<T> clazz)
    {
        return events.last(clazz, event -> true);
    }

    public <T extends ConnectionEvent> T last(final Class<T> clazz, long connectionId)
    {
        return events.last(clazz, event -> event.connectionId() == connectionId);
    }

    public <T extends ConnectionEvent> T last(final Class<T> clazz, long connectionId, final Predicate<T> predicate)
    {
        return events.last(clazz, event -> event.connectionId() == connectionId && predicate.test(event));
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        events.onEvent(event);
    }
}
