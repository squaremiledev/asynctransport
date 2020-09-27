package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.app.TransportEventsListener;

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
