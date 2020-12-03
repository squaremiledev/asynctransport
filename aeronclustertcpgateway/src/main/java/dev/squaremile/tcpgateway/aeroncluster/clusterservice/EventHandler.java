package dev.squaremile.tcpgateway.aeroncluster.clusterservice;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.IdleStrategy;


import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;

public interface EventHandler
{
    int streamId();

    void onStart(IdleStrategy idleStrategy);

    void onSessionOpen(ClientSession session, long timestamp);

    void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason);

    void onSessionMessage(long sessionId, long timestamp, DirectBuffer buffer, int offset, int length);
}
