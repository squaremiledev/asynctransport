package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.serialization.api.SerializedCommandListener;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;

public class RingBufferWriter implements SerializedEventListener, SerializedCommandListener
{
    private final String role;
    private final RingBuffer ringBuffer;

    public RingBufferWriter(final String role, final RingBuffer ringBuffer)
    {
        this.role = role;
        this.ringBuffer = ringBuffer;
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        ringBuffer.write(1, sourceBuffer, sourceOffset, length);
    }

    @Override
    public String toString()
    {
        return "RingBufferWriter{" +
               "role='" + role + '\'' +
               '}';
    }
}
