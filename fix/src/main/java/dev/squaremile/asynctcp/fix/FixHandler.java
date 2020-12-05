package dev.squaremile.asynctcp.fix;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public interface FixHandler
{
    void onMessage(ConnectionTransport transport, MessageReceived messageReceived);
}
