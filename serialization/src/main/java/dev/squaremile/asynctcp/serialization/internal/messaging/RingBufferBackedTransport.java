package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;

public class RingBufferBackedTransport implements MessageDrivenTransport
{
    private final RingBufferReader bufferReader;
    private final MessageDrivenTransport messageDrivenTransport;

    public RingBufferBackedTransport(final MessageDrivenTransport messageDrivenTransport, final RingBuffer toNetworkBuffer)
    {
        this.messageDrivenTransport = messageDrivenTransport;
        bufferReader = new RingBufferReader("toNetwork", toNetworkBuffer, this.messageDrivenTransport);
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void work()
    {
        bufferReader.read();
        messageDrivenTransport.work();
    }

    @Override
    public void close()
    {
        messageDrivenTransport.close();
    }
}
