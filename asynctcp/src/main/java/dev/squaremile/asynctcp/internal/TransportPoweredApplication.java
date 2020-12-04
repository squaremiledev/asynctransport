package dev.squaremile.asynctcp.internal;

import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

public class TransportPoweredApplication implements TransportApplicationOnDuty
{
    private final Transport transport;
    private final TransportApplicationOnDuty app;

    TransportPoweredApplication(final Transport transport, final TransportApplicationOnDuty app)
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
