package dev.squaremile.asynctcp.transport.testfixtures;

import dev.squaremile.asynctcp.transport.internal.domain.StatusEvent;
import dev.squaremile.asynctcp.transport.internal.domain.StatusEventListener;

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
