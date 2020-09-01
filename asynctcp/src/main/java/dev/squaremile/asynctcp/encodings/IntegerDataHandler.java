package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;

class IntegerDataHandler implements ReceivedDataHandler
{

    private final FixedLengthDataHandler fixedLengthDataHandler;

    IntegerDataHandler(final long connectionId, final MessageListener messageListener)
    {
        fixedLengthDataHandler = new FixedLengthDataHandler(connectionId, messageListener);
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
               '}';
    }
}
