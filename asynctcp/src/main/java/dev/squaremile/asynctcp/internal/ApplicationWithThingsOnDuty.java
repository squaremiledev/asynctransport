package dev.squaremile.asynctcp.internal;

import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.OnDuty;

public class ApplicationWithThingsOnDuty implements EventDrivenApplication
{
    private final EventDrivenApplication delegate;
    private final OnDuty additionalThingOnDuty;

    public ApplicationWithThingsOnDuty(final EventDrivenApplication delegate, final OnDuty additionalThingOnDuty)
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
