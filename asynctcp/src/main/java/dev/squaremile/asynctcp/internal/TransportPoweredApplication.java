package dev.squaremile.asynctcp.internal;

import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;

public class TransportPoweredApplication implements Application
{
    private final Transport transport;
    private final Application app;

    TransportPoweredApplication(final Transport transport, final Application app)
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
