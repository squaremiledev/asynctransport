package dev.squaremile.asynctcp.application;

import dev.squaremile.asynctcp.domain.api.Transport;

public class TransportApplication
{
    private final Transport transport;
    private final Application app;

    TransportApplication(final Transport transport, final Application app)
    {
        this.transport = transport;
        this.app = app;
    }

    public void work()
    {
        transport.work();
    }

    public void onStart()
    {
        app.onStart();
    }

    public void onStop()
    {
        app.onStop();
    }
}
