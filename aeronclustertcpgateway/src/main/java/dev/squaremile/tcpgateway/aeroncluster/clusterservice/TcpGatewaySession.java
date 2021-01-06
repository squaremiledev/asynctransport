package dev.squaremile.tcpgateway.aeroncluster.clusterservice;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.IdleStrategy;


import dev.squaremile.asynctcp.internal.serialization.SerializingTransport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplication;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.transport.app.TransportEvent;
import io.aeron.cluster.service.ClientSession;

class TcpGatewaySession
{
    private final TransportApplication transportApplication;
    private final SerializingTransport transport;

    public TcpGatewaySession(
            final ClientSession session,
            final TransportApplicationFactory applicationFactory,
            final IdleStrategy idleStrategy
    )
    {
        this.transport = new SerializingTransport(new ExpandableArrayBuffer(), 0, (sourceBuffer, sourceOffset, length) ->
        {
            idleStrategy.reset();
            while (session.offer(sourceBuffer, sourceOffset, length) < 0)
            {
                idleStrategy.idle();
            }
        });
        this.transportApplication = applicationFactory.create(transport);
    }

    public void onEvent(TransportEvent event)
    {
        transport.onEvent(event);
        transportApplication.onEvent(event);
    }

    public void onStart()
    {
        transportApplication.onStart();
    }
}
