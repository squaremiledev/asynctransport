package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.ConnectionEvent;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.connection.ConnectionEventsListener;

public class SingleByteEncoding implements ConnectionEventsListener
{
    private final EventListener eventListener;

    public SingleByteEncoding(final EventListener eventListener)
    {
        this.eventListener = eventListener;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        eventListener.onEvent(event);
    }
}
