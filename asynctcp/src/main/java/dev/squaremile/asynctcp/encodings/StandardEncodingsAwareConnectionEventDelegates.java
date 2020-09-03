package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.events.ConnectionEvent;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.connection.ConnectionEventsListener;

import static dev.squaremile.asynctcp.domain.api.StandardEncoding.valueOf;

public class StandardEncodingsAwareConnectionEventDelegates
{
    public ConnectionEventsListener createFor(final ConnectionIdValue connectionId, String encoding, EventListener eventListener)
    {
        StandardEncoding standardEncoding = valueOf(encoding);
        switch (standardEncoding)
        {
            case RAW_STREAMING:
                return new Delegation(eventListener, new RawStreamingEncoding(eventListener));
            case SINGLE_BYTE:
                return new Delegation(eventListener, new SingleByteDataHandler(connectionId, eventListener::onEvent));
            case LONGS:
                return new Delegation(eventListener, new LongDataHandler(connectionId, eventListener::onEvent));
            case FOUR_KB:
                return new Delegation(eventListener, new FixedLengthDataHandler(connectionId, eventListener::onEvent, 4 * 1024));
            default:
                throw new IllegalStateException("Unexpected encoding value: " + encoding);
        }
    }

    public static class Delegation implements ConnectionEventsListener
    {
        private final EventListener eventListener;
        private final ReceivedDataHandler receivedDataHandler;

        Delegation(final EventListener eventListener, final ReceivedDataHandler receivedDataHandler)
        {
            this.eventListener = eventListener;
            this.receivedDataHandler = receivedDataHandler;
        }

        @Override
        public void onEvent(final ConnectionEvent event)
        {
            if (event instanceof DataReceived)
            {
                receivedDataHandler.onDataReceived((DataReceived)event);
            }
            else
            {
                eventListener.onEvent(event);
            }
        }
    }
}
