package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;
import java.util.function.Consumer;


import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

class SingleByte
{
    private final byte[] oneByteFlyweight = new byte[1];
    private final ByteBuffer oneByteByteBuffer = ByteBuffer.wrap(oneByteFlyweight);
    private final Consumer<MessageReceived> eventListener;
    private final MessageReceived messageReceivedFlyweight = new MessageReceived();

    SingleByte(final Consumer<MessageReceived> eventListener)
    {
        this.eventListener = eventListener;
    }

    void delineate(final DataReceived dataReceived)
    {
        int length = dataReceived.length();
        for (int i = 0; i < length; i++)
        {
            oneByteFlyweight[0] = dataReceived.data().get(i);
            eventListener.accept(messageReceivedFlyweight.set(dataReceived, oneByteByteBuffer, 1));
        }
    }
}
