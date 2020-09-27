package dev.squaremile.asynctcp.transport.internal.transportencoding;

import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public interface MessageListener
{
    void onMessage(final MessageReceived messageReceived);
}
