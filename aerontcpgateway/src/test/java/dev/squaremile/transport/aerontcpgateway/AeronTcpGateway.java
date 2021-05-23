package dev.squaremile.transport.aerontcpgateway;

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
    private ExclusivePublication aeronPublication;
    private Aeron aeron;

    public AeronTcpGateway(final AeronConnection aeronConnection)
    {
        this.aeronConnection = aeronConnection;
    }

    void connect()
    {
        final Aeron aeron = Aeron.connect(aeronConnection.aeronContext());
        final Subscription aeronSubscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
        final ExclusivePublication aeronPublication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

        this.transport = new AsyncTcp().createTransport(
                "aeron <-> tcp",
                new AeronBackedCommandSupplier(aeronSubscription),
                new AeronSerializedEventPublisher(aeronPublication)
        );
        this.aeronPublication = aeronPublication;
        this.aeron = aeron;
    }

    public boolean isConnected()
    {
        return aeronPublication != null && aeronPublication.isConnected();
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
