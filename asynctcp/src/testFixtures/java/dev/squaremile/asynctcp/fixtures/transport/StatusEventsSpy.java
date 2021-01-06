package dev.squaremile.asynctcp.fixtures.transport;

import dev.squaremile.asynctcp.internal.transport.domain.StatusEvent;
import dev.squaremile.asynctcp.internal.transport.domain.StatusEventListener;

public final class StatusEventsSpy extends Spy<StatusEvent> implements StatusEventListener
{
    private final CapturedItems<StatusEvent> items;

    public StatusEventsSpy()
    {
        this(new CapturedItems<>());
    }

    private StatusEventsSpy(final CapturedItems<StatusEvent> items)
    {
        super(items);
        this.items = items;
    }

    @Override
    public void onEvent(final StatusEvent event)
    {
        items.add(event.copy());
    }
}
