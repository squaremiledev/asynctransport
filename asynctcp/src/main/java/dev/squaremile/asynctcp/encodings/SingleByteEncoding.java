package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.domain.api.events.ConnectionEvent;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;
import dev.squaremile.asynctcp.domain.connection.ConnectionEventsListener;

public class SingleByteEncoding implements ConnectionEventsListener
{
    private final EventListener eventListener;
    private final MessageReceived messageReceived = new MessageReceived();
    private ByteBuffer singleByteByteBuffer = ByteBuffer.wrap(new byte[1]);

    public SingleByteEncoding(final EventListener eventListener)
    {
        this.eventListener = eventListener;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof DataReceived)
        {
            DataReceived dataReceivedEvent = (DataReceived)event;
            ByteBuffer srcBuffer = dataReceivedEvent.data();
            while (srcBuffer.hasRemaining())
            {
                byte b = srcBuffer.get();
                singleByteByteBuffer.clear();
                singleByteByteBuffer.put(b);
                eventListener.onEvent(messageReceived.set(event, singleByteByteBuffer, 1));
            }
        }
        else
        {
            eventListener.onEvent(event);
        }
    }
}
