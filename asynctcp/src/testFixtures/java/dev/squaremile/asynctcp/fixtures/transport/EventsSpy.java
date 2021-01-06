package dev.squaremile.asynctcp.fixtures.transport;

import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.EventListener;

public final class EventsSpy extends Spy<Event> implements EventListener
{
    private final EventListener delegate;
    private final CapturedItems<Event> items;

    private EventsSpy(final EventListener delegate)
    {
        this(delegate, new CapturedItems<>());
    }

    private EventsSpy(final EventListener delegate, final CapturedItems<Event> items)
    {
        super(items);
        this.delegate = delegate;
        this.items = items;
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
        items.add(event.copy());
        delegate.onEvent(event);
    }


}
