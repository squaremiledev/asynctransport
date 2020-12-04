package dev.squaremile.asynctcp.api;

import dev.squaremile.asynctcp.internal.RingBufferBackedTransportApplicationFactory;
import dev.squaremile.asynctcp.serialization.api.SerializedCommandListener;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.api.SerializedMessageListener;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedCommandSupplier;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedEventSupplier;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;

public class AsyncTcp implements TransportApplicationFactory
{
    private final TransportApplicationFactory factory = new RingBufferBackedTransportApplicationFactory();

    @Override
    public TransportApplicationOnDuty create(
            final String role, final int buffersSize, final SerializedMessageListener serializedMessageListener, final TransportApplicationOnDutyFactory applicationFactory
    )
    {
        return factory.create(role, buffersSize, serializedMessageListener, applicationFactory);
    }

    @Override
    public TransportApplicationOnDuty createSharedStack(final String role, final TransportApplicationOnDutyFactory applicationFactory)
    {
        return factory.createSharedStack(role, applicationFactory);
    }

    @Override
    public TransportApplicationOnDuty createWithoutTransport(
            final String role,
            final TransportApplicationOnDutyFactory applicationFactory,
            final SerializedEventSupplier eventSupplier,
            final SerializedCommandListener commandListener,
            final SerializedEventListener serializedEventListener
    )
    {
        return factory.createWithoutTransport(role, applicationFactory, eventSupplier, commandListener, serializedEventListener);
    }

    @Override
    public TransportOnDuty createTransport(
            final String role, final SerializedCommandSupplier commandSupplier, final SerializedEventListener eventListener
    )
    {
        return factory.createTransport(role, commandSupplier, eventListener);
    }
}
