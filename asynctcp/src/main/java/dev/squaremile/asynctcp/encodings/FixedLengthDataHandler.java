package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;

class FixedLengthDataHandler implements ReceivedDataHandler
{
    private static final int ENCODED_INTEGER_LENGTH = 4;
    private final MessageReceived messageReceived = new MessageReceived();
    private final long connectionId; // to show that it's single connection scoped
    private final MessageListener messageListener;
    private final ByteBuffer integerByteBuffer = ByteBuffer.wrap(new byte[4]);

    FixedLengthDataHandler(final long connectionId, final MessageListener messageListener)
    {
        this.connectionId = connectionId;
        this.messageListener = messageListener;
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        ByteBuffer sourceBuffer = event.data();
        int sourceLength = event.length();
        for (int i = 0; i < sourceLength; i++)
        {
            integerByteBuffer.put(sourceBuffer.get());
            if (integerByteBuffer.position() == ENCODED_INTEGER_LENGTH)
            {
                messageListener.onMessage(messageReceived.set(event, integerByteBuffer, ENCODED_INTEGER_LENGTH));
                integerByteBuffer.clear();
            }
        }
    }

    @Override
    public String toString()
    {
        return "IntegerDataHandler{" +
               ", connectionId=" + connectionId +
               '}';
    }
}
