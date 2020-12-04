package dev.squaremile.tcpgateway.aeroncluster.clusterservice;

import java.util.function.Consumer;

import org.agrona.DirectBuffer;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.concurrent.IdleStrategy;


import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.serialization.internal.TransportEventsDeserialization;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;

public class TcpGatewayClient implements EventHandler
{
    private final Long2ObjectHashMap<SerializedEventListener> tcpEventsForTcpGatewaySession = new Long2ObjectHashMap<>();
    private final int streamId;
    private final Consumer<SerializingTransport> onStart;
    private final Consumer<TransportEvent> onTcpEvent;
    private IdleStrategy idleStrategy;

    public TcpGatewayClient(
            final int egressStreamId,
            final Consumer<SerializingTransport> onStart,
            final Consumer<TransportEvent> onTcpEvent
    )
    {
        this.streamId = egressStreamId;
        this.onStart = onStart;
        this.onTcpEvent = onTcpEvent;
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
                idleStrategy,
                onStart::accept,
                onTcpEvent
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
