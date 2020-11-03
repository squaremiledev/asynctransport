package dev.squaremile.asynctcp.internal;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.SystemEpochClock;


import dev.squaremile.asynctcp.api.TransportFactory;
import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.NonBLockingMessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.internal.SerializingApplication;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedCommandSupplier;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedMessageDrivenTransport;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

public class NonProdGradeTransportFactory implements TransportFactory
{
    @Override
    public MessageDrivenTransport create(final String role, final SerializedEventListener eventListener)
    {
        DelineationApplication delineationApplication = new DelineationApplication(
                new SerializingApplication(new ExpandableArrayBuffer(), 0, eventListener)
        );
        return new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        delineationApplication,
                        delineationApplication,
                        new SystemEpochClock(),
                        role
                ));
    }

    @Override
    public MessageDrivenTransport create(
            final String role,
            final SerializedCommandSupplier commandSupplier,
            final SerializedEventListener eventListener
    )
    {
        return new SerializedMessageDrivenTransport(create(role, eventListener), commandSupplier);
    }
}
