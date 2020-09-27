package dev.squaremile.asynctcp.transport.internal.transportencoding;

import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

class LongDataHandler implements ReceivedDataHandler
{
    private final FixedLengthDataHandler fixedLengthDataHandler;
    private final ConnectionIdValue connectionId;

    LongDataHandler(final ConnectionId connectionId, final MessageListener messageListener)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.fixedLengthDataHandler = new FixedLengthDataHandler(connectionId, messageListener, 8);
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        fixedLengthDataHandler.onDataReceived(event);
    }

    @Override
    public String toString()
    {
        return "IntegerDataHandler{" +
               "fixedLengthDataHandler=" + fixedLengthDataHandler +
               ", connectionId=" + connectionId +
               '}';
    }
}
