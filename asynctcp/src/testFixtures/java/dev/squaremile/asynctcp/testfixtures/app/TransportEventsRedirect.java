package dev.squaremile.asynctcp.testfixtures.app;

import dev.squaremile.asynctcp.domain.api.events.Event;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.api.events.TransportEvent;
import dev.squaremile.asynctcp.domain.api.events.TransportEventsListener;

public class TransportEventsRedirect implements EventListener
{
    private TransportEventsListener listener;

    public TransportEventsRedirect(final TransportEventsListener serverEvents)
    {
        listener = serverEvents;
    }

    @Override
    public void onEvent(final Event event)
    {
        if (event instanceof TransportEvent)
        {
            listener.onEvent((TransportEvent)event);
        }
    }
}
