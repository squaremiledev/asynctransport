package dev.squaremile.asynctcp.internal;

import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.OnDuty;

public class ApplicationWithThingsOnDuty implements Application
{
    private final Application delegate;
    private final OnDuty additionalThingOnDuty;

    public ApplicationWithThingsOnDuty(final Application delegate, final OnDuty additionalThingOnDuty)
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
