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
import dev.squaremile.asynctcp.transport.api.values.DelineationType;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

public class NonProdGradeTransportFactory implements TransportFactory
{
    @Override
    public MessageDrivenTransport createMessageDrivenTransport(
            final String role,
            final DelineationType predefinedTransportDelineation,
            final SerializedEventListener serializedEventListener
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
