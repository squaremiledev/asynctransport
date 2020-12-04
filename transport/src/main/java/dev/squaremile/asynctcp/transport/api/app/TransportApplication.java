package dev.squaremile.asynctcp.transport.api.app;

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
