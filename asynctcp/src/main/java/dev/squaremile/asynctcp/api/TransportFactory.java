package dev.squaremile.asynctcp.api;

import java.io.IOException;

import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;

public interface TransportFactory
{
    MessageDrivenTransport create(final String role, final SerializedEventListener eventListener) throws IOException;

    MessageDrivenTransport create(final String role, final RingBuffer networkToUser, final RingBuffer userToNetwork) throws IOException;
}
