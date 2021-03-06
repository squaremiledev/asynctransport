package dev.squaremile.transport.aeroncluster.fixtures.applications;

import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.IdleStrategy;


import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;

public class AccumulatorClusteredService implements ClusteredService
{
    private final MutableDirectBuffer sendBuffer = new ExpandableDirectByteBuffer(128);
    private IdleStrategy idleStrategy;
    private long totalSoFar;

    public void onStart(final Cluster cluster, final Image snapshotImage)
    {
        System.out.println("[onStart]");
        this.idleStrategy = cluster.idleStrategy();
    }

    public void onSessionOpen(final ClientSession session, final long timestamp)
    {
        System.out.println("[onSessionOpen]");

    }

    public void onSessionClose(final ClientSession session, final long timestamp, final CloseReason closeReason)
    {
        System.out.println("[onSessionClose]");

    }

    public void onTimerEvent(final long correlationId, final long timestamp)
    {
        System.out.println("[onTimerEvent]");
    }

    public void onTakeSnapshot(final ExclusivePublication snapshotPublication)
    {
        System.out.println("[onTakeSnapshot]");
    }

    public void onRoleChange(final Cluster.Role newRole)
    {
        System.out.println("[onRoleChange]");
    }

    public void onTerminate(final Cluster cluster)
    {
        System.out.println("[onTerminate]");
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
        System.out.println("[onSessionMessage]");
        final int message = buffer.getInt(offset);
        totalSoFar += message;
        if (null != session)
        {
            sendBuffer.putInt(0, message);
            sendBuffer.putLong(BitUtil.SIZE_OF_INT, totalSoFar);
            idleStrategy.reset();
            while (session.offer(sendBuffer, 0, BitUtil.SIZE_OF_INT + BitUtil.SIZE_OF_LONG) < 0)
            {
                idleStrategy.idle();
            }
        }
    }
}
