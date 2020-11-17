package dev.squaremile.asynctcp.fix.examplecertification;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;

public class IgnoreAll implements ConnectionApplication
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
