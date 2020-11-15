package dev.squaremile.fix.certification;

import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;

public class IgnoreAll implements FakeApplication
{

    @Override
    public void onEvent(final ConnectionEvent event)
    {
    }

    @Override
    public void work()
    {

    }
}
