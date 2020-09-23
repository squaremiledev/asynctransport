package dev.squaremile.asynctcp.internal.transportencoding;

import dev.squaremile.asynctcp.api.events.MessageReceived;

public interface MessageListener
{
    void onMessage(final MessageReceived messageReceived);
}
