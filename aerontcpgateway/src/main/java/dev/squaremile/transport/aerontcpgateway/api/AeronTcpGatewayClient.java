package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import io.aeron.Aeron;
import io.aeron.ExclusivePublication;
import io.aeron.Subscription;

public class AeronTcpGatewayClient implements OnDuty
{
    private final AeronConnection aeronConnection;
    private Aeron aeron;
    private ExclusivePublication publication;
    private TransportApplicationOnDuty application;
    private Subscription subscription;

    public AeronTcpGatewayClient(final AeronConnection aeronConnection)
    {
        this.aeronConnection = aeronConnection;
    }

    public void connect()
    {
        final Aeron aeron = Aeron.connect(aeronConnection.aeronContext());
        this.subscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());
        this.publication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
        this.aeron = aeron;
    }

    public TransportApplicationOnDuty create(
            final String role,
            final TransportApplicationOnDutyFactory applicationFactory,
            final SerializedEventListener serializedEventListener
    )
    {
        if (aeron == null)
        {
            throw new IllegalStateException("Connect to the Aeron Gateway first");
        }
        return new AsyncTcp().createWithoutTransport(
                role,
                applicationFactory,
                new SubscribedMessageSupplier(subscription)::poll,
                new SerializedMessagePublisher(publication)::onSerialized,
                serializedEventListener
        );
    }

    public void startApplication(
            final String role,
            final TransportApplicationOnDutyFactory applicationFactory,
            final SerializedEventListener serializedEventListener
    )
    {
        this.application = create(role, applicationFactory, serializedEventListener);
        this.application.onStart();
    }

    public boolean isConnected()
    {
        return publication != null && publication.isConnected();
    }

    public void close()
    {
        if (aeron != null)
        {
            aeron.close();
        }
        if (application != null)
        {
            application.close();
        }
    }

    @Override
    public void work()
    {
        if (application != null)
        {
            application.work();
        }
    }
}
