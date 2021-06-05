package dev.squaremile.asynctcp.internal.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.transport.app.TransportEvent;
import dev.squaremile.asynctcp.api.transport.app.TransportEventsListener;

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
        TransportEvent decodedEvent = decoders.decode(sourceBuffer, sourceOffset, length);
        transportEventsListener.onEvent(decodedEvent);
    }
}
