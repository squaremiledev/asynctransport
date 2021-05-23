package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportOnDuty;
import io.aeron.Aeron;
import io.aeron.ExclusivePublication;
import io.aeron.Subscription;

public class AeronTcpGateway implements OnDuty, AutoCloseable
{
    private final AeronConnection aeronConnection;
    private TransportOnDuty transport;
    private ExclusivePublication publication;
    private Aeron aeron;

    public AeronTcpGateway(final AeronConnection aeronConnection)
    {
        this.aeronConnection = aeronConnection;
    }

    void connect()
    {
        final Aeron aeron = Aeron.connect(aeronConnection.aeronContext());
        final Subscription subscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
        final ExclusivePublication publication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

        this.transport = new AsyncTcp().createTransport(
                "aeron <-> tcp",
                new SubscribedMessageSupplier(subscription)::poll,
                new SerializedMessagePublisher(publication)::onSerialized
        );
        this.publication = publication;
        this.aeron = aeron;
    }

    public boolean isConnected()
    {
        return publication != null && publication.isConnected();
    }

    @Override
    public void work()
    {
        if (transport != null)
        {
            transport.work();
        }
    }

    @Override
    public void close()
    {
        if (transport != null)
        {
            transport.close();
        }
        if (aeron != null)
        {
            aeron.close();
        }
    }
}
