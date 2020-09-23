package dev.squaremile.asynctcp.testfixtures;

import dev.squaremile.asynctcp.internal.domain.StatusEvent;
import dev.squaremile.asynctcp.internal.domain.StatusEventListener;

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
