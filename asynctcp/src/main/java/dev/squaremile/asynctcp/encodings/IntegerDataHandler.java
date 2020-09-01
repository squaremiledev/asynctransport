package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;

class IntegerDataHandler implements ReceivedDataHandler
{
    private final FixedLengthDataHandler fixedLengthDataHandler;
    private final long connectionId;

    IntegerDataHandler(final long connectionId, final MessageListener messageListener)
    {
        this.connectionId = connectionId;
        this.fixedLengthDataHandler = new FixedLengthDataHandler(messageListener, 4);
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
