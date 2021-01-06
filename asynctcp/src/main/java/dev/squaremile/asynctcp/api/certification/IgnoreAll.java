package dev.squaremile.asynctcp.api.certification;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;

public class IgnoreAll implements ConnectionApplication
{
    @Override
    public void onEvent(final ConnectionEvent event)
    {
    }
}
