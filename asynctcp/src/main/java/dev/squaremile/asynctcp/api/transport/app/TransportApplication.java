package dev.squaremile.asynctcp.api.transport.app;

public interface TransportApplication extends ApplicationLifecycle, EventListener
{
    @Override
    default void onStart()
    {

    }

    @Override
    default void onStop()
    {

    }
}
