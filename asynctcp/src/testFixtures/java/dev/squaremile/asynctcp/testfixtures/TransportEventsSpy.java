package dev.squaremile.asynctcp.testfixtures;

import dev.squaremile.asynctcp.domain.api.events.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.domain.api.events.TransportEvent;
import dev.squaremile.asynctcp.domain.api.events.TransportEventsListener;

public final class TransportEventsSpy extends EventsSpy<TransportEvent> implements TransportEventsListener
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
