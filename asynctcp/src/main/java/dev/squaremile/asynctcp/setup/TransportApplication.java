package dev.squaremile.asynctcp.setup;

import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Transport;

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
