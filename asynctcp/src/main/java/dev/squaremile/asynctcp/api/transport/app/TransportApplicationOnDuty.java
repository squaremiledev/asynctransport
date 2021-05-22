package dev.squaremile.asynctcp.api.transport.app;

public interface TransportApplicationOnDuty extends TransportApplication, ApplicationOnDuty, EventListener
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

    @Override
    void onEvent(Event event);
}
