package dev.squaremile.asynctcp.domain.connection;

import dev.squaremile.asynctcp.domain.api.events.ConnectionEvent;

public interface ConnectionEventsListener
{
    void onEvent(ConnectionEvent event);
}
