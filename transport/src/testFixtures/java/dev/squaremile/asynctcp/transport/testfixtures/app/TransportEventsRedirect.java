package dev.squaremile.asynctcp.transport.testfixtures.app;

import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEventsListener;

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
