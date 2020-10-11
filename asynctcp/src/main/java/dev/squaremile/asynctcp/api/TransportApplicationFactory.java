package dev.squaremile.asynctcp.api;

import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;

public interface TransportApplicationFactory
{
    Application create(String role, ApplicationFactory applicationFactory);

    Application create(String role, RingBuffer networkToUser, RingBuffer userToNetwork, ApplicationFactory applicationFactory);
}
