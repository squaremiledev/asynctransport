package dev.squaremile.asynctcp.transport.api.app;

public interface EventDrivenApplication extends ApplicationOnDuty, EventListener
{
    @Override
    default void onStart()
    {

    }

    @Override
    default void onStop()
    {

    }

    @Override
    default void work()
    {

    }
}
