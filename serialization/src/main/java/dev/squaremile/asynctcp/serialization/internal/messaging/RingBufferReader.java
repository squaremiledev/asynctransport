package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.concurrent.MessageHandler;
import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.serialization.internal.SerializedMessageListener;

public class RingBufferReader
{
    private final RingBuffer ringBuffer;
    private MessageHandler messageHandler;

    public RingBufferReader(final RingBuffer ringBuffer, final SerializedMessageListener serializedMessageListener)
    {
        this.ringBuffer = ringBuffer;
        this.messageHandler = (msgTypeId, buffer, index, length) -> serializedMessageListener.onSerialized(buffer, index, length);
    }

    public int read()
    {
        return ringBuffer.read(messageHandler);
    }
}
