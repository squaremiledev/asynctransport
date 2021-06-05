package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import io.aeron.Aeron;
import io.aeron.ExclusivePublication;

public class AeronTcpGatewayClient implements AutoCloseable
{
    private final AeronConnection aeronConnection;
    private Aeron aeron;

    public AeronTcpGatewayClient(final AeronConnection aeronConnection)
    {
        this.aeronConnection = aeronConnection;
    }

    public AeronTcpGatewayClient start()
    {
        aeron = Aeron.connect(aeronConnection.aeronContext());
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

        final ExclusivePublication publication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
        while (!publication.isConnected())
        {

        }
        final SerializedMessagePublisher serializedMessagePublisher = new SerializedMessagePublisher(publication);
        return new AsyncTcp().createWithoutTransport(
                role,
                applicationFactory,
                new SubscribedMessageSupplier(aeron.addSubscription(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId()))::poll,
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

