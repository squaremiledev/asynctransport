package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class ApplicationResolver implements ConnectionApplication
{
    private static final NoOpConnectionApplication NO_APPLICATION = new NoOpConnectionApplication();
    private final ConnectionId connectionId;
    private final LazyConnectionApplicationFactory lazyConnectionApplicationFactory;
    private ConnectionApplication delegate = NO_APPLICATION;

    public ApplicationResolver(final ConnectionId connectionId, final LazyConnectionApplicationFactory lazyConnectionApplicationFactory)
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
        ConnectionApplication connectionApplication = lazyConnectionApplicationFactory.onStart();
        if (connectionApplication != null)
        {
            this.delegate = connectionApplication;
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

    private static class NoOpConnectionApplication implements ConnectionApplication
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
}
