package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;

class OnEventConnectionApplication implements ConnectionApplication
{
    private static final NoOpConnectionApplication NO_APPLICATION = new NoOpConnectionApplication();
    private final ConnectionTransport connectionTransport;
    private final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory;
    private ConnectionApplication delegate = NO_APPLICATION;
    private boolean isResolved = false;

    public OnEventConnectionApplication(
            final ConnectionTransport connectionTransport,
            final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory
    )
    {
        this.connectionTransport = connectionTransport;
        this.onEventConnectionApplicationFactory = onEventConnectionApplicationFactory;
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
