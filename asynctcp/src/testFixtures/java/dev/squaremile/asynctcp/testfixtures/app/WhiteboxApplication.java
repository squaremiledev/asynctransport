package dev.squaremile.asynctcp.testfixtures.app;

import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Event;
import dev.squaremile.asynctcp.api.app.Transport;
import dev.squaremile.asynctcp.api.app.TransportEventsListener;

/**
 * A standard app should by autonomous and reactive.
 * It should not require external control apart from the injection of dependencies via a constructor.
 * This is not a standard app. All the logic lives outside it to show the transport API in the tests.
 */
public class WhiteboxApplication<L extends TransportEventsListener> implements Application
{
    private final L events;
    private final TransportEventsRedirect eventsRedirect;
    private final Transport transport;


    public WhiteboxApplication(final Transport transport, final L transportEventsListener)
    {
        this.transport = transport;
        this.events = transportEventsListener;
        this.eventsRedirect = new TransportEventsRedirect(events);
    }

    @Override
    public void onEvent(final Event event)
    {
        eventsRedirect.onEvent(event);
    }

    public Transport underlyingtTansport()
    {
        return transport;
    }

    public L events()
    {
        return events;
    }

    @Override
    public void work()
    {
        transport.work();
    }
}
