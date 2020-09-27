package dev.squaremile.asynctcp.transport.setup;

import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Transport;

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
        app.work();
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
