package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;

class LongDataHandler implements ReceivedDataHandler
{
    private final FixedLengthDataHandler fixedLengthDataHandler;
    private final long connectionId;

    LongDataHandler(final long connectionId, final MessageListener messageListener)
    {
        this.connectionId = connectionId;
        this.fixedLengthDataHandler = new FixedLengthDataHandler(messageListener, 8);
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
