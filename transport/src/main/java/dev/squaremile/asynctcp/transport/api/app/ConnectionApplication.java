package dev.squaremile.asynctcp.transport.api.app;

import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionEventsListener;

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
