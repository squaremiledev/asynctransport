package dev.squaremile.asynctcp.api;

import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.transport.api.app.OnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;

public interface TransportFactory
{
    TransportOnDuty create(final String role, final SerializedEventListener eventListener);

    TransportOnDuty create(final String role, final RingBuffer networkToUser, final RingBuffer userToNetwork);
}
