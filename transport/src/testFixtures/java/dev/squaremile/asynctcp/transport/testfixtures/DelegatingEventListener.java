package dev.squaremile.asynctcp.transport.testfixtures;

import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEventsListener;
import dev.squaremile.asynctcp.transport.internal.domain.StatusEvent;
import dev.squaremile.asynctcp.transport.internal.domain.StatusEventListener;

public class DelegatingEventListener implements EventListener
{
    private final TransportEventsListener transportEventsListener;
    private final StatusEventListener statusEventListener;

    DelegatingEventListener(final TransportEventsListener transportEventsListener, final StatusEventListener statusEventListener)
    {
        this.transportEventsListener = transportEventsListener;
        this.statusEventListener = statusEventListener;
    }

    @Override
    public void onEvent(final Event event)
    {
        if (event instanceof TransportEvent)
        {
            transportEventsListener.onEvent((TransportEvent)event);
        }
        else if (event instanceof StatusEvent)
        {
            statusEventListener.onEvent((StatusEvent)event);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported type " + event.getClass());
        }
    }
}
