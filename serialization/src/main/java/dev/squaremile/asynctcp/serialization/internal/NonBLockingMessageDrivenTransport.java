package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.transport.api.app.Transport;

public class NonBLockingMessageDrivenTransport implements MessageDrivenTransport
{
    private final Transport transport;
    private final TransportCommandDeserialization deserialization;

    public NonBLockingMessageDrivenTransport(final Transport transport)
    {
        this.transport = transport;
        this.deserialization = new TransportCommandDeserialization(transport);
    }


    @Override
    public void work()
    {
        transport.work();
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        deserialization.onSerialized(sourceBuffer, sourceOffset, length);
    }

    @Override
    public void close()
    {
        transport.close();
    }
}
