package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;

class NoOpConnectionApplication implements ConnectionApplication
{

    @Override
    public void onStart()
    {

    }

    @Override
    public void onStop()
    {

    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {

    }
}
