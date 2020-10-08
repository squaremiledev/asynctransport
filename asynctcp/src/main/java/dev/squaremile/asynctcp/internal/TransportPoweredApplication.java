package dev.squaremile.asynctcp.internal;

import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;

public class TransportPoweredApplication implements Application
{
    private final TransportOnDuty transport;
    private final Application app;

    TransportPoweredApplication(final TransportOnDuty transport, final Application app)
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
