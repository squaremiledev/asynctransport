package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.connection.ConnectionEventsListener;

import static dev.squaremile.asynctcp.domain.api.StandardEncoding.valueOf;

public class StandardEncodingsAwareConnectionEventDelegates
{
    public ConnectionEventsListener createFor(String encoding, EventListener eventListener)
    {
        StandardEncoding standardEncoding = valueOf(encoding);
        switch (standardEncoding)
        {
            case RAW_STREAMING:
                return eventListener::onEvent;
            case SINGLE_BYTE:
                return new SingleByteEncoding(eventListener);
            default:
                throw new IllegalStateException("Unexpected value: " + encoding);
        }
    }

}
