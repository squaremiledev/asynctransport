package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.ConnectionEvent;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;
import dev.squaremile.asynctcp.domain.connection.ConnectionEventsListener;

public class SingleByteEncoding implements ConnectionEventsListener
{
    private final EventListener eventListener;
    private final MessageReceived messageReceived = new MessageReceived();

    public SingleByteEncoding(final EventListener eventListener)
    {
        this.eventListener = eventListener;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof DataReceived)
        {
            eventListener.onEvent(messageReceived.set(((DataReceived)event)));
        }
        else
        {
            eventListener.onEvent(event);
        }
    }
}
