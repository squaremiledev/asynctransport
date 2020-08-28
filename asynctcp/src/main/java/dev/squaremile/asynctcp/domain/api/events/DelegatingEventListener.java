package dev.squaremile.asynctcp.domain.api.events;

public class DelegatingEventListener implements EventListener
{
    private final TransportEventsListener transportEventsListener;
    private final StatusEventListener statusEventListener;

    public DelegatingEventListener(final TransportEventsListener transportEventsListener, final StatusEventListener statusEventListener)
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
