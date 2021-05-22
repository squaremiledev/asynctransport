package dev.squaremile.transport.aeron;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.serialization.SerializedCommandListener;
import io.aeron.ExclusivePublication;

class AeronSerializedCommandPublisher implements SerializedCommandListener
{
    private final ExclusivePublication publication;

    public AeronSerializedCommandPublisher(final ExclusivePublication publication)
    {
        this.publication = publication;
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        publication.offer(sourceBuffer, sourceOffset, length);
    }
}
