package dev.squaremile.asynctcp.api;

import dev.squaremile.asynctcp.internal.RingBufferBackedTransportApplicationFactory;
import dev.squaremile.asynctcp.api.serialization.SerializedCommandListener;
import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedCommandSupplier;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedEventSupplier;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.app.TransportOnDuty;

public class AsyncTcp implements TransportApplicationFactory
{
    private final TransportApplicationFactory factory = new RingBufferBackedTransportApplicationFactory();

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportApplicationOnDuty create(
            final String role, final int buffersSize, final SerializedMessageListener serializedMessageListener, final TransportApplicationOnDutyFactory applicationFactory
    )
    {
        return factory.create(role, buffersSize, serializedMessageListener, applicationFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportApplicationOnDuty createSharedStack(final String role, final TransportApplicationOnDutyFactory applicationFactory)
    {
        return factory.createSharedStack(role, applicationFactory);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportOnDuty createTransport(
            final String role, final SerializedCommandSupplier commandSupplier, final SerializedEventListener eventListener
    )
    {
        return factory.createTransport(role, commandSupplier, eventListener);
    }
}
