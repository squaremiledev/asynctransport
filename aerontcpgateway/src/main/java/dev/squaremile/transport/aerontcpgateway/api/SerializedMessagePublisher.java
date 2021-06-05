package dev.squaremile.transport.aerontcpgateway.api;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import io.aeron.ExclusivePublication;

class SerializedMessagePublisher implements SerializedMessageListener
{
    private final ExclusivePublication publication;

    public SerializedMessagePublisher(final ExclusivePublication publication)
    {
        this.publication = publication;
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        long offer = Long.MIN_VALUE;
        do
        {
            if (publication.isConnected())
            {
                offer = publication.offer(sourceBuffer, sourceOffset, length);
            }
        }
        while (offer < 0);
    }
}
