package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;

class BuySideTransportApplication implements ConnectionApplication
{

    public BuySideTransportApplication(final BuySideApplication application)
    {
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
    }

}
