package dev.squaremile.asynctcp.internal;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;


import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.serialization.api.SerializedCommandListener;
import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationValidatingTransport;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedEventDrivenApplication;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedEventSupplier;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

import static dev.squaremile.asynctcp.transport.api.app.TransportCommandHandler.NO_HANDLER;

public class NonProdGradeTransportAppFactory implements TransportApplicationFactory
{
    private static final int MSG_TYPE_ID = 1;

    private final NonProdGradeTransportFactory transportFactory = new NonProdGradeTransportFactory();

    @Override
    public EventDrivenApplication create(final String role, final int buffersSize, final ApplicationFactory applicationFactory)
    {
        final RingBuffer networkToUser = new OneToOneRingBuffer(new UnsafeBuffer(new byte[buffersSize + TRAILER_LENGTH]));
        final RingBuffer userToNetwork = new OneToOneRingBuffer(new UnsafeBuffer(new byte[buffersSize + TRAILER_LENGTH]));
        return new ApplicationWithThingsOnDuty(
                createWithoutTransport(
                        role,
                        applicationFactory,
                        networkToUser::read,
                        (sourceBuffer, sourceOffset, length) -> userToNetwork.write(MSG_TYPE_ID, sourceBuffer, sourceOffset, length)
                ),
                transportFactory.create(
                        "networkFacing",
                        userToNetwork::read,
                        (sourceBuffer, sourceOffset, length) -> networkToUser.write(MSG_TYPE_ID, sourceBuffer, sourceOffset, length)
                )
        );
    }

    @Override
    public EventDrivenApplication create(final String role, ApplicationFactory applicationFactory)
    {
        ListeningApplication listeningApplication = new ListeningApplication();
        Transport transport = new DelineationValidatingTransport(listeningApplication, new NonBlockingTransport(listeningApplication, NO_HANDLER, System::currentTimeMillis, role));
        EventDrivenApplication app = new DelineationApplication(applicationFactory.create(transport));
        listeningApplication.set(app);
        return new TransportPoweredApplication(transport, app);
    }

    @Override
    public EventDrivenApplication createWithoutTransport(
            final String role,
            final ApplicationFactory applicationFactory,
            final SerializedEventSupplier eventSupplier,
            final SerializedCommandListener commandListener
    )
    {
        final SerializingTransport serializingTransport = new SerializingTransport(new ExpandableArrayBuffer(), 0, commandListener);
        return new SerializedEventDrivenApplication(serializingTransport, applicationFactory.create(serializingTransport), eventSupplier);
    }


    private static class ListeningApplication implements EventListener
    {
        private EventDrivenApplication listeningApplication;

        @Override
        public void onEvent(final Event event)
        {
            listeningApplication.onEvent(event);
        }

        public void set(final EventDrivenApplication app)
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
