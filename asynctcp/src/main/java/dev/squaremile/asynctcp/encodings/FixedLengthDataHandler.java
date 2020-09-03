package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.MessageListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;

public class FixedLengthDataHandler implements ReceivedDataHandler
{
    private final MessageReceived messageReceived;
    private final ConnectionIdValue connectionId;
    private final MessageListener messageListener;
    private final ByteBuffer messageBuffer;
    private int messageLength;

    public FixedLengthDataHandler(final ConnectionId connectionId, final MessageListener messageListener, final int messageLength)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.messageListener = messageListener;
        this.messageLength = messageLength;
        this.messageBuffer = ByteBuffer.wrap(new byte[messageLength]);
        this.messageReceived = new MessageReceived(connectionId);
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        ByteBuffer sourceBuffer = event.data();
        int sourceLength = event.length();
        for (int i = 0; i < sourceLength; i++)
        {
            // TODO [perf]: think about a zero copy alternative (source buffer with offset based)
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
               ", connectionId=" + connectionId +
               ", messageListener=" + messageListener +
               ", messageBuffer=" + messageBuffer +
               ", messageLength=" + messageLength +
               '}';
    }
}
