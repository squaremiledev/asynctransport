package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.api.events.StatusEvent;
import com.michaelszymczak.sample.sockets.api.events.StatusEventListener;

public final class SynchronizedStatusEventsSpy extends EventsSpy<StatusEvent> implements StatusEventListener
{
    private final SynchronizedCapturedItems<StatusEvent> items;

    public SynchronizedStatusEventsSpy()
    {
        this(new SynchronizedCapturedItems<>());
    }

    private SynchronizedStatusEventsSpy(final SynchronizedCapturedItems<StatusEvent> items)
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
