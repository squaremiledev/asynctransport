package dev.squaremile.asynctcp.api;

import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationEmittingEventsFactory;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;

public interface TransportApplicationFactory
{
    Application create(
            final String role,
            ApplicationFactory applicationFactory
    );

    Application create(
            final String role,
            final OneToOneRingBuffer networkToUserRingBuffer,
            final OneToOneRingBuffer userToNetworkRingBuffer,
            ApplicationEmittingEventsFactory applicationFactory
    );
}
