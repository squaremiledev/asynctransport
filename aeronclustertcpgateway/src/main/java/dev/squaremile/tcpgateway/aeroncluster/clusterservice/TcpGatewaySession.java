package dev.squaremile.tcpgateway.aeroncluster.clusterservice;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.IdleStrategy;


import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import io.aeron.cluster.service.ClientSession;

class TcpGatewaySession
{
    private final EventDrivenApplication eventDrivenApplication;
    private final SerializingTransport transport;

    public TcpGatewaySession(
            final ClientSession session,
            final ApplicationFactory applicationFactory,
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
        this.eventDrivenApplication = applicationFactory.create(transport);
    }

    public void onEvent(TransportEvent event)
    {
        transport.onEvent(event);
        eventDrivenApplication.onEvent(event);
    }

    public void onStart()
    {
        eventDrivenApplication.onStart();
    }
}
