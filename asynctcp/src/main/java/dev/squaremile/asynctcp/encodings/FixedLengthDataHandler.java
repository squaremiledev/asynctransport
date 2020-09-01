package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;

class FixedLengthDataHandler implements ReceivedDataHandler
{
    private final MessageReceived messageReceived;
    private final MessageListener messageListener;
    private final ByteBuffer messageBuffer;
    private int messageLength;

    FixedLengthDataHandler(final MessageListener messageListener, final int messageLength)
    {
        this.messageListener = messageListener;
        this.messageLength = messageLength;
        this.messageBuffer = ByteBuffer.wrap(new byte[messageLength]);
        this.messageReceived = new MessageReceived();
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        ByteBuffer sourceBuffer = event.data();
        int sourceLength = event.length();
        for (int i = 0; i < sourceLength; i++)
        {
            // TODO: think about a zero copy alternative (source buffer with offset based)
            messageBuffer.put(sourceBuffer.get());
            if (messageBuffer.position() == messageLength)
            {
                messageListener.onMessage(messageReceived.set(event, messageBuffer, messageLength));
                messageBuffer.clear();
            }
        }
    }

    @Override
    public String toString()
    {
        return "FixedLengthDataHandler{" +
               "messageReceived=" + messageReceived +
               ", messageListener=" + messageListener +
               ", messageBuffer=" + messageBuffer +
               ", messageLength=" + messageLength +
               '}';
    }
}
