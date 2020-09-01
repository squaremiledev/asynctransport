package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;

class IntegerDataHandler implements ReceivedDataHandler
{
    public static final int ENCODED_INTEGER_LENGTH = 4;
    private final MessageReceived messageReceived = new MessageReceived();
    private final long connectionId; // to show that it's single connection scoped
    private final MessageListener messageListener;
    private ByteBuffer integerByteBuffer = ByteBuffer.wrap(new byte[4]);

    IntegerDataHandler(final long connectionId, final MessageListener messageListener)
    {
        this.connectionId = connectionId;
        this.messageListener = messageListener;
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        if (event.length() != 4)
        {
            return;
        }
        ByteBuffer data = event.data();
        for (int i = 0; i < ENCODED_INTEGER_LENGTH; i++)
        {
            integerByteBuffer.put(data.get());
        }

        messageListener.onMessage(messageReceived.set(event, integerByteBuffer, ENCODED_INTEGER_LENGTH));
    }

    @Override
    public String toString()
    {
        return "IntegerDataHandler{" +
               ", connectionId=" + connectionId +
               '}';
    }
}
