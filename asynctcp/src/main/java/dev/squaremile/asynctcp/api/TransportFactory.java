package dev.squaremile.asynctcp.api;

import java.io.IOException;

import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;


import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;

public interface TransportFactory
{
    MessageDrivenTransport createMessageDrivenTransport(
            final String role,
            final SerializedEventListener serializedEventListener
    ) throws IOException;

    MessageDrivenTransport createRingBufferDrivenTransport(
            final String role,
            final OneToOneRingBuffer networkToUserRingBuffer,
            final OneToOneRingBuffer userToNetworkRingBuffer

    ) throws IOException;
}
