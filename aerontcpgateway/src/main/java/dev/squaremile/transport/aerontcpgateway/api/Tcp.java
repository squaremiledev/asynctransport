package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import io.aeron.Aeron;
import io.aeron.ExclusivePublication;

public class Tcp implements AutoCloseable
{
    private final DriverConfiguration driverConfiguration;
    private Aeron aeron;

    public Tcp(final DriverConfiguration driverConfiguration)
    {
        this.driverConfiguration = driverConfiguration;
    }

    public Tcp start()
    {
        aeron = Aeron.connect(driverConfiguration.aeronContext());
        return this;
    }

    public TransportApplicationOnDuty create(
            final String role,
            final TransportApplicationOnDutyFactory applicationFactory,
            final SerializedMessageListener serializedMessageListener
    )
    {
        if (aeron == null)
        {
            throw new IllegalStateException("Start the client before creating an application");
        }

        final ExclusivePublication publication = aeron.addExclusivePublication(driverConfiguration.channel(), driverConfiguration.toNetworAeronStreamId());
        while (!publication.isConnected())
        {

        }
        final SerializedMessagePublisher serializedMessagePublisher = new SerializedMessagePublisher(role + " publisher", publication);
        return new AsyncTcp().createWithoutTransport(
                role,
                applicationFactory,
                new SubscribedMessageSupplier(aeron.addSubscription(driverConfiguration.channel(), driverConfiguration.fromNetworAeronStreamId()))::poll,
                (sourceBuffer, sourceOffset, length) ->
                {
                    serializedMessagePublisher.onSerialized(sourceBuffer, sourceOffset, length);
                    serializedMessageListener.onSerialized(sourceBuffer, sourceOffset, length);
                },
                serializedMessageListener::onSerialized
        );
    }

    @Override
    public void close()
    {
        if (aeron != null)
        {
            aeron.close();
        }
    }
}