package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class StartedConnectionApplication implements ConnectionApplication
{
    private static final NoOpConnectionApplication NO_APPLICATION = new NoOpConnectionApplication();
    private final ConnectionId connectionId;
    private final LazyConnectionApplicationFactory lazyConnectionApplicationFactory;
    private ConnectionApplication delegate = NO_APPLICATION;
    private boolean isResolved = false;

    public StartedConnectionApplication(final ConnectionId connectionId, final LazyConnectionApplicationFactory lazyConnectionApplicationFactory)
    {
        this.connectionId = connectionId;
        this.lazyConnectionApplicationFactory = lazyConnectionApplicationFactory;
    }

    @Override
    public ConnectionId connectionId()
    {
        return connectionId;
    }

    @Override
    public void onStart()
    {
        if (!isResolved)
        {
            delegate = lazyConnectionApplicationFactory.onStart();
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
