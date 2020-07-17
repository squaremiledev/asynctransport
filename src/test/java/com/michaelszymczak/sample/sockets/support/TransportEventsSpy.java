package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.api.events.TransportCorrelatedEvent;
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;

public final class TransportEventsSpy extends EventsSpy<TransportEvent> implements TransportEventsListener
{
    private final CapturedItems<TransportEvent> items;

    TransportEventsSpy()
    {
        this(new CapturedItems<>());
    }

    private TransportEventsSpy(final CapturedItems<TransportEvent> items)
    {
        super(items);
        this.items = items;
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
}
