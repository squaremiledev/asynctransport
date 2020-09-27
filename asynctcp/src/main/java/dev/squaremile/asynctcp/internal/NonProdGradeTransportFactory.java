package dev.squaremile.asynctcp.internal;

import java.io.IOException;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.SystemEpochClock;


import dev.squaremile.asynctcp.api.TransportFactory;
import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.MessageEncodingApplication;
import dev.squaremile.asynctcp.serialization.internal.NonBLockingMessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.internal.SerializingApplication;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportEncoding;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

public class NonProdGradeTransportFactory implements TransportFactory
{
    @Override
    public MessageDrivenTransport createMessageDrivenTransport(
            final String role,
            final PredefinedTransportEncoding predefinedTransportEncoding,
            final SerializedEventListener serializedEventListener
    ) throws IOException
    {
        return new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        new MessageEncodingApplication(
                                new SerializingApplication(
                                        new ExpandableArrayBuffer(),
                                        0,
                                        serializedEventListener
                                ),
                                predefinedTransportEncoding
                        ),
                        new SystemEpochClock(),
                        role
                ));
    }
}
