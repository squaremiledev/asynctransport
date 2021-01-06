package dev.squaremile.asynctcp.internal;

import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;

public class ApplicationWithThingsOnDuty implements TransportApplicationOnDuty
{
    private final TransportApplicationOnDuty delegate;
    private final OnDuty additionalThingOnDuty;

    public ApplicationWithThingsOnDuty(final TransportApplicationOnDuty delegate, final OnDuty additionalThingOnDuty)
    {
        this.delegate = delegate;
        this.additionalThingOnDuty = additionalThingOnDuty;
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
        additionalThingOnDuty.work();

    }

    @Override
    public void onEvent(final Event event)
    {
        delegate.onEvent(event);
    }
}
