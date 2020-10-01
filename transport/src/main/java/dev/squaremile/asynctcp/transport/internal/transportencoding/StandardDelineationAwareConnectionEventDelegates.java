package dev.squaremile.asynctcp.transport.internal.transportencoding;

import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionEventsListener;

import static dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation.valueOf;

public class StandardDelineationAwareConnectionEventDelegates
{
    public ConnectionEventsListener createFor(final ConnectionIdValue connectionId, String delineation, EventListener eventListener)
    {
        PredefinedTransportDelineation predefinedTransportDelineation = valueOf(delineation);
        switch (predefinedTransportDelineation)
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
                throw new IllegalStateException("Unexpected delineation value: " + delineation);
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
