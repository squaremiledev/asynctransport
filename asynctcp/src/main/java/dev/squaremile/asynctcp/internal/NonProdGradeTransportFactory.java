package dev.squaremile.asynctcp.internal;

import java.io.IOException;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.SystemEpochClock;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;


import dev.squaremile.asynctcp.api.TransportFactory;
import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.NonBLockingMessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.internal.SerializingApplication;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.serialization.internal.messaging.RingBufferBackedTransport;
import dev.squaremile.asynctcp.serialization.internal.messaging.RingBufferWriter;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

public class NonProdGradeTransportFactory implements TransportFactory
{
    @Override
    public MessageDrivenTransport createRingBufferDrivenTransport(
            final String role,
            final OneToOneRingBuffer networkToUserRingBuffer,
            final OneToOneRingBuffer userToNetworkRingBuffer

    ) throws IOException
    {
        return new RingBufferBackedTransport(
                createMessageDrivenTransport(
                        role,
                        new RingBufferWriter("networkToUserRingBuffer", networkToUserRingBuffer)
                ),
                userToNetworkRingBuffer
        );
    }

    @Override
    public MessageDrivenTransport createMessageDrivenTransport(
            final String role, final SerializedEventListener serializedEventListener
    ) throws IOException
    {
        DelineationApplication delineationApplication = new DelineationApplication(
                new SerializingApplication(
                        new ExpandableArrayBuffer(),
                        0,
                        serializedEventListener
                )
        );
        return new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        delineationApplication,
                        delineationApplication,
                        new SystemEpochClock(),
                        role
                ));
    }
}
