package dev.squaremile.asynctcp.internal;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationValidatingTransport;
import dev.squaremile.asynctcp.serialization.internal.messaging.RingBufferApplication;
import dev.squaremile.asynctcp.serialization.internal.messaging.RingBufferWriter;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

import static dev.squaremile.asynctcp.transport.api.app.TransportCommandHandler.NO_HANDLER;

public class NonProdGradeTransportAppFactory implements TransportApplicationFactory
{

    private final NonProdGradeTransportFactory transportFactory = new NonProdGradeTransportFactory();

    @Override
    public EventDrivenApplication create(final String role, final RingBuffer networkToUser, final RingBuffer userToNetwork, final ApplicationFactory applicationFactory)
    {
        return new ApplicationWithThingsOnDuty(
                createWithoutTransport(
                        role,
                        networkToUser,
                        userToNetwork,
                        applicationFactory
                ),
                transportFactory.create(
                        "networkFacing",
                        networkToUser,
                        userToNetwork
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
    public EventDrivenApplication createWithoutTransport(final String role, final RingBuffer networkToUser, final RingBuffer userToNetwork, final ApplicationFactory applicationFactory)
    {
        SerializingTransport serializingTransport = new SerializingTransport(new ExpandableArrayBuffer(), 0, new RingBufferWriter("userToNetworkRingBuffer", userToNetwork));
        return new RingBufferApplication(serializingTransport, applicationFactory.create(serializingTransport), networkToUser);
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
