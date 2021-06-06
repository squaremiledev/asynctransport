package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportOnDuty;
import io.aeron.Aeron;
import io.aeron.ExclusivePublication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class AeronTcpGateway implements OnDuty, AutoCloseable
{
    private final int toNetworkStreamId;
    private final int fromNetworStreamId;
    private AeronConnection aeronConnection;
    private TransportOnDuty transport;
    private ExclusivePublication publication;
    private Aeron aeron;
    private MediaDriver mediaDriver;
    private String aeronDirectoryName;

    public AeronTcpGateway(final int toNetworkStreamId, final int fromNetworStreamId)
    {
        this(toNetworkStreamId, fromNetworStreamId, null);
    }

    public AeronTcpGateway(final int toNetworkStreamId, final int fromNetworStreamId, String aeronDirectoryName)
    {
        this.toNetworkStreamId = toNetworkStreamId;
        this.fromNetworStreamId = fromNetworStreamId;
        this.aeronDirectoryName = aeronDirectoryName;
    }

    public boolean hasClientConnected()
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
            transport = null;
        }
        if (aeron != null)
        {
            aeron.close();
            aeron = null;
        }
        if (mediaDriver != null)
        {
            mediaDriver.close();
            mediaDriver = null;
        }
    }

    public AeronTcpGateway start()
    {
        if (aeronDirectoryName == null)
        {
            this.mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED).dirDeleteOnShutdown(true));
            this.aeronDirectoryName = mediaDriver.aeronDirectoryName();
        }
        this.aeronConnection = new AeronConnection(toNetworkStreamId, fromNetworStreamId, aeronDirectoryName);
        this.aeron = Aeron.connect(aeronConnection.aeronContext());

        final Subscription subscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
        this.publication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());
        this.transport = new AsyncTcp().createTransport(
                "aeron <-> tcp",
                new SubscribedMessageSupplier(subscription)::poll,
                new SerializedMessagePublisher(publication)::onSerialized
        );
        return this;
    }

    public AeronConnection aeronConnection()
    {
        if (aeronConnection == null)
        {
            throw new IllegalStateException("start the gateway to retrieve the connection details");
        }
        return aeronConnection;
    }
}
