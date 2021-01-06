package dev.squaremile.tcpgateway.aeroncluster.clusterservice;

import org.agrona.DirectBuffer;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.concurrent.IdleStrategy;


import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.internal.serialization.TransportEventsDeserialization;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationFactory;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;

public class TcpGatewayClient implements EventHandler
{
    private final Long2ObjectHashMap<SerializedEventListener> tcpEventsForTcpGatewaySession = new Long2ObjectHashMap<>();
    private final int streamId;
    private final TransportApplicationFactory applicationFactory;
    private IdleStrategy idleStrategy;

    public TcpGatewayClient(
            final int egressStreamId,
            final TransportApplicationFactory applicationFactory
    )
    {
        this.streamId = egressStreamId;
        this.applicationFactory = applicationFactory;
    }

    @Override
    public int streamId()
    {
        return streamId;
    }

    @Override
    public void onStart(IdleStrategy idleStrategy)
    {
        this.idleStrategy = idleStrategy;
    }

    @Override
    public void onSessionOpen(final ClientSession session, final long timestamp)
    {
        final TcpGatewaySession tcpGatewaySession = new TcpGatewaySession(
                session,
                applicationFactory,
                idleStrategy
        );
        tcpEventsForTcpGatewaySession.put(session.id(), new TransportEventsDeserialization(tcpGatewaySession::onEvent));
        tcpGatewaySession.onStart();
    }

    @Override
    public void onSessionClose(final ClientSession session, final long timestamp, final CloseReason closeReason)
    {
        tcpEventsForTcpGatewaySession.remove(session.id());
    }

    @Override
    public void onSessionMessage(final long sessionId, final long timestamp, final DirectBuffer buffer, final int offset, final int length)
    {
        tcpEventsForTcpGatewaySession.get(sessionId).onSerialized(buffer, offset, length);
    }
}
