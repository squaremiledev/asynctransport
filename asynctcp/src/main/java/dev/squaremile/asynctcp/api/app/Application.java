package dev.squaremile.asynctcp.api.app;

public interface Application extends EventListener, OnDuty
{
    default void onStart()
    {

    }

    default void onStop()
    {

    }
}
