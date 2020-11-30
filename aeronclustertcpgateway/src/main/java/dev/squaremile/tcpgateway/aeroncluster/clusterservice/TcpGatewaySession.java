package dev.squaremile.tcpgateway.aeroncluster.clusterservice;

import java.util.function.Consumer;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.IdleStrategy;


import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import io.aeron.cluster.service.ClientSession;

class TcpGatewaySession
{
    private final SerializingTransport transport;
    private final Consumer<TransportEvent> onTcpEvent;
    private final Consumer<SerializingTransport> onStart;

    public TcpGatewaySession(
            final ClientSession session,
            final IdleStrategy idleStrategy,
            final Consumer<SerializingTransport> onStart,
            final Consumer<TransportEvent> onTcpEvent
    )
    {
        this.onTcpEvent = onTcpEvent;
        this.transport = new SerializingTransport(new ExpandableArrayBuffer(), 0, (sourceBuffer, sourceOffset, length) ->
        {
            idleStrategy.reset();
            while (session.offer(sourceBuffer, sourceOffset, length) < 0)
            {
                idleStrategy.idle();
            }
        });
        this.onStart = onStart;
    }

    public void onEvent(TransportEvent event)
    {
        onTcpEvent.accept(event);
        transport.onEvent(event);
    }

    public void onStart()
    {
        onStart.accept(transport);
    }
}
