package dev.squaremile.asynctcp.testfitures.app;

import dev.squaremile.asynctcp.application.Application;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.events.Event;
import dev.squaremile.asynctcp.testfitures.TransportEventsSpy;

/**
 * A standard app should by autonomous and reactive.
 * It should not require external control apart from the injection of dependencies via a constructor.
 * This is not a standard app. All the logic lives outside it to show the transport API in the tests.
 */
public class WhiteboxApplication implements Application
{
    private final TransportEventsSpy events = new TransportEventsSpy();
    private final TransportEventsRedirect eventsRedirect = new TransportEventsRedirect(events);
    private final Transport transport;

    public WhiteboxApplication(final Transport transport)
    {
        this.transport = transport;
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

    public TransportEventsSpy events()
    {
        return events;
    }
}
