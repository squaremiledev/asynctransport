package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;

class SingleByteDataHandler implements ReceivedDataHandler
{
    private final MessageReceived messageReceivedFlyweight;
    private final ConnectionIdValue connectionId; // to show that it's single connection scoped
    private final MessageListener messageListener;
    private ByteBuffer singleByteByteBuffer = ByteBuffer.wrap(new byte[1]);

    SingleByteDataHandler(final ConnectionId connectionId, final MessageListener messageListener)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.messageListener = messageListener;
        this.messageReceivedFlyweight = new MessageReceived(connectionId);
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        ByteBuffer srcBuffer = event.data();
        while (srcBuffer.hasRemaining())
        {
            byte b = srcBuffer.get();
            singleByteByteBuffer.clear();
            singleByteByteBuffer.put(b);
            MessageReceived messageReceived = this.messageReceivedFlyweight.set(event, singleByteByteBuffer, 1);
            messageListener.onMessage(messageReceived);
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
