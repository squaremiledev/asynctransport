package dev.squaremile.asynctcp.application;

import dev.squaremile.asynctcp.domain.api.events.EventListener;

public interface Application extends EventListener
{
    default void onStart()
    {

    }

    default void onStop()
    {

    }
}
