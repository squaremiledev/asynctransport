package dev.squaremile.asynctcp.playground;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.app.Transport;
import dev.squaremile.asynctcp.internal.nonblockingimpl.NonBlockingTransport;
import dev.squaremile.asynctcp.serialization.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.TransportCommandDeserialization;

public class NonBLockingMessageDrivenTransport implements MessageDrivenTransport
{
    private final Transport transport;
    private final TransportCommandDeserialization deserialization;

    public NonBLockingMessageDrivenTransport(final NonBlockingTransport transport)
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
