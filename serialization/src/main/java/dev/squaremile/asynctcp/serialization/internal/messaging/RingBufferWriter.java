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
        // TODO: not quite clear why I have to add 4 here, but if I don't add anything it does not read write whole message
        // TODO: the root cause is probably wrong length calculation in the layer above for 2 messages with var data encoding
        // TODO: but it is not clear to me how exactly it should look like
        ringBuffer.write(1, sourceBuffer, sourceOffset, length + 4);
    }

    @Override
    public String toString()
    {
        return "RingBufferWriter{" +
               "role='" + role + '\'' +
               '}';
    }
}
