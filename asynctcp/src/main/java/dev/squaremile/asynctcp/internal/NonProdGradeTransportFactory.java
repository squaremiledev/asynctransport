package dev.squaremile.asynctcp.internal;

import java.io.IOException;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.SystemEpochClock;


import dev.squaremile.asynctcp.api.TransportFactory;
import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.NonBLockingMessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.internal.SerializingApplication;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

public class NonProdGradeTransportFactory implements TransportFactory
{
    @Override
    public MessageDrivenTransport createMessageDrivenTransport(
            final String role,
            final PredefinedTransportDelineation predefinedTransportDelineation,
            final SerializedEventListener serializedEventListener
    ) throws IOException
    {
        return new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        new DelineationApplication(
                                new SerializingApplication(
                                        new ExpandableArrayBuffer(),
                                        0,
                                        serializedEventListener
                                ),
                                predefinedTransportDelineation
                        ),
                        new SystemEpochClock(),
                        role
                ));
    }
}
