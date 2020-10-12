package dev.squaremile.asynctcp.internal;

import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;

public class TransportPoweredApplication implements EventDrivenApplication
{
    private final Transport transport;
    private final EventDrivenApplication app;

    TransportPoweredApplication(final Transport transport, final EventDrivenApplication app)
    {
        this.transport = transport;
        this.app = app;
    }

    @Override
    public void work()
    {
        app.work();
        transport.work();
    }

    @Override
    public void onStart()
    {
        app.onStart();
        transport.work();
    }

    @Override
    public void onStop()
    {
        app.onStop();
        transport.work();
    }

    @Override
    public void onEvent(final Event event)
    {
        app.onEvent(event);
    }
}
