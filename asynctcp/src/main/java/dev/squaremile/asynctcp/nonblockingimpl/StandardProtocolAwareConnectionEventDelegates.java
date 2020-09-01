package dev.squaremile.asynctcp.nonblockingimpl;

import dev.squaremile.asynctcp.domain.api.StandardProtocol;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.connection.ConnectionEventsListener;

public class StandardProtocolAwareConnectionEventDelegates
{
    ConnectionEventsListener createFor(String protocol, EventListener eventListener)
    {
        if (StandardProtocol.valueOf(protocol) == StandardProtocol.RAW_STREAMING)
        {
            return eventListener::onEvent;
        }
        throw new IllegalArgumentException("Unsupported protocol " + protocol);
    }
}
