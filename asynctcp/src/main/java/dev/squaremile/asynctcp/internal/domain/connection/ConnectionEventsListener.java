package dev.squaremile.asynctcp.internal.domain.connection;

import dev.squaremile.asynctcp.api.app.ConnectionEvent;

public interface ConnectionEventsListener
{
    void onEvent(ConnectionEvent event);
}
