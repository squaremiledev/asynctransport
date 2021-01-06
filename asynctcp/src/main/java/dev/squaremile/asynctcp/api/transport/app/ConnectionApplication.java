package dev.squaremile.asynctcp.api.transport.app;

import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionEventsListener;

public interface ConnectionApplication extends ConnectionEventsListener, OnDuty
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
