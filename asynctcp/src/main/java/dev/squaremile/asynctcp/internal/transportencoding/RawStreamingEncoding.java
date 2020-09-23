package dev.squaremile.asynctcp.internal.transportencoding;

import dev.squaremile.asynctcp.api.app.EventListener;
import dev.squaremile.asynctcp.api.events.DataReceived;

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
