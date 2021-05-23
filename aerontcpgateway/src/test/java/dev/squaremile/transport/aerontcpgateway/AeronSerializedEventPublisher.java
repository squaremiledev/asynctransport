package dev.squaremile.transport.aerontcpgateway;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import io.aeron.ExclusivePublication;

class AeronSerializedEventPublisher implements SerializedEventListener
{
    private final ExclusivePublication publication;

    public AeronSerializedEventPublisher(final ExclusivePublication publication)
    {
        this.publication = publication;
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        publication.offer(sourceBuffer, sourceOffset, length);
    }
}
