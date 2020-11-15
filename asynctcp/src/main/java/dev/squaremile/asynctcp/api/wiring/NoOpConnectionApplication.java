package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

class NoOpConnectionApplication implements ConnectionApplication
{
    @Override
    public ConnectionId connectionId()
    {
        return null;
    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onStop()
    {

    }

    @Override
    public void work()
    {

    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {

    }
}