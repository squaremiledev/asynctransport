package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.EventListener;

public class RawStreamingEncoding implements ReceivedDataHandler
{
    private final EventListener eventListener;

    public RawStreamingEncoding(final EventListener eventListener)
    {
        this.eventListener = eventListener;
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        eventListener.onEvent(event);
    }
}
