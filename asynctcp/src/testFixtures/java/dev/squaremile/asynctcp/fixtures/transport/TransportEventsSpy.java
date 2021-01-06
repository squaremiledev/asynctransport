package dev.squaremile.asynctcp.fixtures.transport;

import dev.squaremile.asynctcp.api.transport.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportEventsListener;

public final class TransportEventsSpy extends Spy<TransportEvent> implements TransportEventsListener
{
    private final CapturedItems<TransportEvent> items;

    public TransportEventsSpy()
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
        items.add(event.copy());
    }
}
