package dev.squaremile.asynctcp.transport.testfixtures;

import dev.squaremile.asynctcp.transport.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEventsListener;

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
