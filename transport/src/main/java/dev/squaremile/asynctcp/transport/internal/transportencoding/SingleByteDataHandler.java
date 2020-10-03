package dev.squaremile.asynctcp.transport.internal.transportencoding;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

class SingleByteDataHandler implements ReceivedDataHandler
{
    private final MessageReceived messageReceivedFlyweight;
    private final ConnectionIdValue connectionId; // to show that it's single connection scoped
    private final MessageListener messageListener;

    SingleByteDataHandler(final ConnectionId connectionId, final MessageListener messageListener)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.messageListener = messageListener;
        this.messageReceivedFlyweight = new MessageReceived(connectionId);
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        DirectBuffer srcBuffer = event.buffer();
        int srcOffset = event.offset();
        int srcLength = event.length();
        for (int i = 0; i < srcLength; i++)
        {
            messageReceivedFlyweight.set(event, srcBuffer, srcOffset + i, 1);
            messageListener.onMessage(messageReceivedFlyweight);
        }
    }

    @Override
    public String toString()
    {
        return "SingleByteDataHandler{" +
               ", connectionId=" + connectionId +
               '}';
    }
}
