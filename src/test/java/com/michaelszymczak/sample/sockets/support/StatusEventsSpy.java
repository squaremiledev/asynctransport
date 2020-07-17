package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.api.events.StatusEvent;
import com.michaelszymczak.sample.sockets.api.events.StatusEventListener;

public final class StatusEventsSpy extends EventsSpy<StatusEvent> implements StatusEventListener
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
        items.add(event);
    }
}
