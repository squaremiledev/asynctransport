package dev.squaremile.asynctcp.internal.transport.domain.connection;

import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;

public interface ConnectionEventsListener
{
    void onEvent(ConnectionEvent event);
}
