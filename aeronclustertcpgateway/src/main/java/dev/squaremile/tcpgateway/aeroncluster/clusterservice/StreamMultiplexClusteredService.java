package dev.squaremile.tcpgateway.aeroncluster.clusterservice;

import java.util.stream.Collectors;

import org.agrona.DirectBuffer;
import org.agrona.collections.Int2ObjectHashMap;


import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;

import static java.util.Arrays.stream;

public class StreamMultiplexClusteredService implements ClusteredService
{
    private final Int2ObjectHashMap<EventHandler> eventHandlers;

    public StreamMultiplexClusteredService(final EventHandler... eventHandlers)
    {
        if (eventHandlers.length != stream(eventHandlers).mapToInt(EventHandler::streamId).distinct().count())
        {
            throw new IllegalArgumentException("Ambiguous unique stream ids: " + stream(eventHandlers).map(EventHandler::streamId).collect(Collectors.toList()));
        }
        this.eventHandlers = new Int2ObjectHashMap<>();
        stream(eventHandlers).forEach(eventHandler -> this.eventHandlers.put(eventHandler.streamId(), eventHandler));
    }

    public void onStart(final Cluster cluster, final Image snapshotImage)
    {
        this.eventHandlers.values().forEach(eventHandler -> eventHandler.onStart(cluster.idleStrategy()));
    }

    public void onSessionOpen(final ClientSession session, final long timestamp)
    {
        if (eventHandlers.containsKey(session.responseStreamId()))
        {
            eventHandlers.get(session.responseStreamId()).onSessionOpen(session, timestamp);
        }
    }

    public void onSessionClose(final ClientSession session, final long timestamp, final CloseReason closeReason)
    {
        if (eventHandlers.containsKey(session.responseStreamId()))
        {
            eventHandlers.get(session.responseStreamId()).onSessionClose(session, timestamp, closeReason);
        }
    }

    public void onSessionMessage(
            final ClientSession session,
            final long timestamp,
            final DirectBuffer buffer,
            final int offset,
            final int length,
            final Header header
    )
    {
        if (eventHandlers.containsKey(session.responseStreamId()))
        {
            eventHandlers.get(session.responseStreamId()).onSessionMessage(session.id(), timestamp, buffer, offset, length);
        }
    }

    public void onTimerEvent(final long correlationId, final long timestamp)
    {
    }

    public void onTakeSnapshot(final ExclusivePublication snapshotPublication)
    {
    }

    public void onRoleChange(final Cluster.Role newRole)
    {
    }

    public void onTerminate(final Cluster cluster)
    {
    }
}
