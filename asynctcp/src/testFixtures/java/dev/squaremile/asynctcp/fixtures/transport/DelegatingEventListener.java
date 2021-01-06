package dev.squaremile.asynctcp.fixtures.transport;

import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.transport.app.TransportEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportEventsListener;
import dev.squaremile.asynctcp.internal.transport.domain.StatusEvent;
import dev.squaremile.asynctcp.internal.transport.domain.StatusEventListener;

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
