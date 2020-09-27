package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.serialization.api.SerializedCommandListener;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;

public class RingBufferWriter implements SerializedEventListener, SerializedCommandListener
{
    private final RingBuffer networkToUserRingBuffer;

    public RingBufferWriter(final RingBuffer networkToUserRingBuffer)
    {
        this.networkToUserRingBuffer = networkToUserRingBuffer;
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        networkToUserRingBuffer.write(1, sourceBuffer, sourceOffset, length);
    }
}
