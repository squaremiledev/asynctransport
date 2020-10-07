package dev.squaremile.asynctcp.transport.api.app;

public interface Application extends EventListener, OnDuty
{
    default void onStart()
    {

    }

    default void onStop()
    {

    }

    @Override
    default void work()
    {

    }
}
