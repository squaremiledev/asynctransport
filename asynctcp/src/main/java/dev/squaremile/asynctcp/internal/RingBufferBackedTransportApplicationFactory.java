package dev.squaremile.asynctcp.internal;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.SystemEpochClock;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;


import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.serialization.MessageDrivenTransport;
import dev.squaremile.asynctcp.api.serialization.SerializedCommandListener;
import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.internal.serialization.NonBLockingMessageDrivenTransport;
import dev.squaremile.asynctcp.internal.serialization.SerializingApplication;
import dev.squaremile.asynctcp.internal.serialization.SerializingTransport;
import dev.squaremile.asynctcp.internal.serialization.delineation.DelineationApplication;
import dev.squaremile.asynctcp.internal.serialization.delineation.DelineationValidatingTransport;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedCommandSupplier;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedEventDrivenApplication;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedEventSupplier;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedMessageDrivenTransport;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.internal.transport.nonblockingimpl.NonBlockingTransport;

import static dev.squaremile.asynctcp.api.transport.app.TransportCommandHandler.NO_HANDLER;

public class RingBufferBackedTransportApplicationFactory implements TransportApplicationFactory
{
    private static final int MSG_TYPE_ID = 1;

    @Override
    public TransportApplicationOnDuty create(
            final String role,
            final int buffersSize,
            final SerializedMessageListener serializedMessageListener,
            final TransportApplicationOnDutyFactory applicationFactory
    )
    {
        final RingBuffer networkToUser = new OneToOneRingBuffer(new UnsafeBuffer(new byte[buffersSize + TRAILER_LENGTH]));
        final RingBuffer userToNetwork = new OneToOneRingBuffer(new UnsafeBuffer(new byte[buffersSize + TRAILER_LENGTH]));
        return new ApplicationWithThingsOnDuty(
                createWithoutTransport(
                        role,
                        applicationFactory,
                        networkToUser::read,
                        (sourceBuffer, sourceOffset, length) ->
                        {
                            boolean success = userToNetwork.write(MSG_TYPE_ID, sourceBuffer, sourceOffset, length);
                            if (!success)
                            {
                                throw new IllegalStateException("Unable to write to the buffer");
                            }
                            serializedMessageListener.onSerialized(sourceBuffer, sourceOffset, length);
                        },
                        serializedMessageListener::onSerialized
                ),
                createTransport(
                        "networkFacing",
                        userToNetwork::read,
                        (sourceBuffer, sourceOffset, length) -> networkToUser.write(MSG_TYPE_ID, sourceBuffer, sourceOffset, length)
                )
        );
    }

    @Override
    public TransportApplicationOnDuty createSharedStack(final String role, TransportApplicationOnDutyFactory applicationFactory)
    {
        ListeningApplication listeningApplication = new ListeningApplication();
        Transport transport = new DelineationValidatingTransport(listeningApplication, new NonBlockingTransport(listeningApplication, NO_HANDLER, System::currentTimeMillis, role));
        TransportApplicationOnDuty app = new DelineationApplication(applicationFactory.create(transport));
        listeningApplication.set(app);
        return new TransportPoweredApplication(transport, app);
    }

    @Override
    public TransportApplicationOnDuty createWithoutTransport(
            final String role,
            final TransportApplicationOnDutyFactory applicationFactory,
            final SerializedEventSupplier eventSupplier,
            final SerializedCommandListener serializedCommandListener,
            final SerializedEventListener serializedEventListener
    )
    {
        final SerializingTransport serializingTransport = new SerializingTransport(new ExpandableArrayBuffer(), 0, serializedCommandListener);
        return new SerializedEventDrivenApplication(serializingTransport, applicationFactory.create(serializingTransport), eventSupplier, serializedEventListener);
    }

    @Override
    public MessageDrivenTransport createTransport(
            final String role,
            final SerializedCommandSupplier commandSupplier,
            final SerializedEventListener eventListener
    )
    {
        final DelineationApplication delineationApplication = new DelineationApplication(
                new SerializingApplication(new ExpandableArrayBuffer(), 0, eventListener)
        );
        return new SerializedMessageDrivenTransport(new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        delineationApplication,
                        delineationApplication,
                        new SystemEpochClock(),
                        role
                )), commandSupplier);
    }

    private static class ListeningApplication implements EventListener
    {
        private TransportApplicationOnDuty listeningApplication;

        @Override
        public void onEvent(final Event event)
        {
            listeningApplication.onEvent(event);
        }

        public void set(final TransportApplicationOnDuty app)
        {
            listeningApplication = app;
        }

        @Override
        public String toString()
        {
            return "ListeningApplication{" +
                   "listeningApplication=" + listeningApplication +
                   '}';
        }
    }
}
