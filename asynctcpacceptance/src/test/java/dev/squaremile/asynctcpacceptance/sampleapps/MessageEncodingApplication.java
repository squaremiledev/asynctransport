package dev.squaremile.asynctcpacceptance.sampleapps;

import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Event;

public class MessageEncodingApplication implements Application
{
    private final Application delegate;

    public MessageEncodingApplication(final Application delegate)
    {

        this.delegate = delegate;
    }

    @Override
    public void onStart()
    {
        delegate.onStart();
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
    public void onEvent(final Event event)
    {
        delegate.onEvent(event);
    }
}
