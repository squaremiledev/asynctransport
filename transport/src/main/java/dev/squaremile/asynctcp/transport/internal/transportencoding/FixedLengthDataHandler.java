package dev.squaremile.asynctcp.transport.internal.transportencoding;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;


import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class FixedLengthDataHandler implements ReceivedDataHandler
{
    private final MessageReceived messageReceived;
    private final ConnectionIdValue connectionId;
    private final MessageListener messageListener;
    private final MutableDirectBuffer messageBuffer;
    private int messageBufferOffset = 0;
    private int messageLength;

    public FixedLengthDataHandler(final ConnectionId connectionId, final MessageListener messageListener, final int messageLength)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.messageListener = messageListener;
        this.messageLength = messageLength;
        this.messageBuffer = new UnsafeBuffer(new byte[messageLength]);
        this.messageReceived = new MessageReceived(connectionId);
    }

    @Override
    public void onDataReceived(final DataReceived event)
    {
        DirectBuffer srcBuffer = event.buffer();
        int srcOffset = event.offset();
        int srcLength = event.length();
        for (int i = 0; i < srcLength; i++)
        {
            messageBufferOffset++;
            messageBuffer.putByte(messageBufferOffset - 1, srcBuffer.getByte(srcOffset + i));
            if (messageBufferOffset == messageLength)
            {
                messageListener.onMessage(messageReceived.set(event, messageBuffer, 0, messageLength));
                messageBufferOffset = 0;
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
