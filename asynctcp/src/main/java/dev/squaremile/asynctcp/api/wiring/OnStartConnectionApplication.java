package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

class OnStartConnectionApplication implements ConnectionApplication
{
    private static final NoOpConnectionApplication NO_APPLICATION = new NoOpConnectionApplication();
    private final ConnectionTransport connectionTransport;
    private final ConnectionId connectionId;
    private final OnStartConnectionApplicationFactory onStartConnectionApplicationFactory;
    private ConnectionApplication delegate = NO_APPLICATION;
    private boolean isResolved = false;

    public OnStartConnectionApplication(
            final ConnectionTransport connectionTransport,
            final ConnectionId connectionId,
            final OnStartConnectionApplicationFactory onStartConnectionApplicationFactory
    )
    {
        this.connectionTransport = connectionTransport;
        this.connectionId = connectionId;
        this.onStartConnectionApplicationFactory = onStartConnectionApplicationFactory;
    }

    @Override
    public void onStart()
    {
        if (!isResolved)
        {
            delegate = onStartConnectionApplicationFactory.createOnStart(connectionTransport, connectionId);
            isResolved = true;
            delegate.onStart();
        }
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
        delegate.onEvent(event);
    }
}
