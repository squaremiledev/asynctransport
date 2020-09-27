package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.transport.api.app.TransportEventsListener;

public class TransportEventsDeserialization implements SerializedEventListener
{
    private final TransportEventDecoders decoders = new TransportEventDecoders();
    private final TransportEventsListener transportEventsListener;

    public TransportEventsDeserialization(final TransportEventsListener transportEventsListener)
    {
        this.transportEventsListener = transportEventsListener;
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        transportEventsListener.onEvent(decoders.decode(sourceBuffer, sourceOffset, length));
    }
}
