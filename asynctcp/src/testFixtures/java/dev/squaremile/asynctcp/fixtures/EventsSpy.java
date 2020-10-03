package dev.squaremile.asynctcp.fixtures;

import java.util.ArrayList;
import java.util.List;


import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;

public final class EventsSpy implements EventListener
{
    private final List<Event> received = new ArrayList<>();
    private final EventListener delegate;

    private EventsSpy(final EventListener delegate)
    {
        this.delegate = delegate;
    }

    public static EventsSpy spy()
    {
        return spyAndDelegateTo(event ->
                                {

                                });
    }

    public static EventsSpy spyAndDelegateTo(final EventListener eventListener)
    {
        return new EventsSpy(eventListener);
    }

    @Override
    public void onEvent(final Event event)
    {
        received.add(event.copy());
        delegate.onEvent(event);
    }

    public List<Event> received()
    {
        return received;
    }
}
