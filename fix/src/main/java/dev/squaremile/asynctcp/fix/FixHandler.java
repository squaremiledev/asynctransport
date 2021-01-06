package dev.squaremile.asynctcp.fix;

import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;

public interface FixHandler
{
    void onMessage(ConnectionTransport transport, MessageReceived messageReceived);
}
