package dev.squaremile.asynctcp.api.transport.app;

public interface TransportApplicationOnDuty extends AutoCloseable, TransportApplication, ApplicationOnDuty, EventListener
{
    TransportApplicationOnDuty NO_OP = event ->
    {

    };

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
    default void close()
    {

    }

    @Override
    void onEvent(Event event);
}
