package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.AutoCloseableOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportOnDuty;
import io.aeron.Aeron;
import io.aeron.ExclusivePublication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class TcpDriver implements AutoCloseableOnDuty
{
    private final int toNetworkStreamId;
    private final int fromNetworStreamId;
    private final boolean launchMediaDriver;
    private DriverConfiguration driverConfiguration;
    private TransportOnDuty transport;
    private ExclusivePublication publication;
    private Aeron aeron;
    private MediaDriver embeddedMediaDriver;
    private String aeronDirectoryName;

    public TcpDriver(final int toNetworkStreamId, final int fromNetworStreamId)
    {
        this(toNetworkStreamId, fromNetworStreamId, true, null);
    }

    public TcpDriver(final int toNetworkStreamId, final int fromNetworStreamId, final boolean launchMediaDriver, String aeronDirectoryName)
    {
        this.toNetworkStreamId = toNetworkStreamId;
        this.fromNetworStreamId = fromNetworStreamId;
        this.aeronDirectoryName = aeronDirectoryName;
        this.launchMediaDriver = launchMediaDriver;
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
        if (embeddedMediaDriver != null)
        {
            embeddedMediaDriver.close();
            embeddedMediaDriver = null;
        }
    }

    public TcpDriver start()
    {
        if (launchMediaDriver)
        {
            final MediaDriver.Context ctx = new MediaDriver.Context().threadingMode(ThreadingMode.SHARED);
            if (aeronDirectoryName == null)
            {
                ctx.dirDeleteOnShutdown(true);
            }
            else
            {
                ctx.dirDeleteOnShutdown(false);
                ctx.aeronDirectoryName(aeronDirectoryName);
            }
            this.embeddedMediaDriver = MediaDriver.launchEmbedded(ctx);
            this.aeronDirectoryName = embeddedMediaDriver.aeronDirectoryName();
        }
        this.driverConfiguration = new DriverConfiguration(toNetworkStreamId, fromNetworStreamId, aeronDirectoryName);
        this.aeron = Aeron.connect(driverConfiguration.aeronContext());

        final Subscription subscription = aeron.addSubscription(driverConfiguration.channel(), driverConfiguration.toNetworAeronStreamId());
        this.publication = aeron.addExclusivePublication(driverConfiguration.channel(), driverConfiguration.fromNetworAeronStreamId());
        this.transport = new AsyncTcp().createTransport(
                "aeron <-> tcp",
                new SubscribedMessageSupplier(subscription)::poll,
                new SerializedMessagePublisher("[tcp -> app] publisher", publication)::onSerialized
        );
        return this;
    }

    public DriverConfiguration configuration()
    {
        if (driverConfiguration == null)
        {
            throw new IllegalStateException("start the driver to retrieve the connection details");
        }
        return driverConfiguration;
    }
}
