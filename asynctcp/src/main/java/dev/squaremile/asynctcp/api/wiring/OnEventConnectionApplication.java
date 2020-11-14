package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

class OnEventConnectionApplication implements ConnectionApplication
{
    private static final NoOpConnectionApplication NO_APPLICATION = new NoOpConnectionApplication();
    private final ConnectionTransport connectionTransport;
    private final ConnectionId connectionId;
    private final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory;
    private ConnectionApplication delegate = NO_APPLICATION;
    private boolean isResolved = false;

    public OnEventConnectionApplication(
            final ConnectionTransport connectionTransport,
            final ConnectionId connectionId,
            final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory
    )
    {
        this.connectionTransport = connectionTransport;
        this.connectionId = connectionId;
        this.onEventConnectionApplicationFactory = onEventConnectionApplicationFactory;
    }

    @Override
    public ConnectionId connectionId()
    {
        return connectionId;
    }

    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
        delegate.onStop();
    }

    @Override
    public void work()
    {
        delegate.work();
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (!isResolved)
        {
            ConnectionApplication connectionApplication = onEventConnectionApplicationFactory.createOnEvent(connectionTransport, event);
            if (connectionApplication != null)
            {
                delegate = connectionApplication;
                isResolved = true;
                delegate.onStart();
            }
        }
        delegate.onEvent(event);
    }
}
