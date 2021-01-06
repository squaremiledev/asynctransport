package dev.squaremile.tcpgateway.aeroncluster.clusterclient;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SystemEpochClock;


import dev.squaremile.asynctcp.internal.serialization.NonBLockingMessageDrivenTransport;
import dev.squaremile.asynctcp.internal.serialization.SerializingApplication;
import dev.squaremile.asynctcp.internal.serialization.delineation.DelineationApplication;
import dev.squaremile.asynctcp.internal.transport.nonblockingimpl.NonBlockingTransport;
import dev.squaremile.transport.aeroncluster.api.ClientFactory;
import dev.squaremile.transport.aeroncluster.api.ClusterClientApplication;
import dev.squaremile.transport.aeroncluster.api.ClusterClientPublisher;
import dev.squaremile.transport.aeroncluster.api.IngressDefinition;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.logbuffer.Header;

public class TcpGatewayConnection
{
    private final ClientFactory clientFactory = new ClientFactory();
    private final IngressDefinition ingress;
    private final int ingressStreamId;
    private final int egressStreamId;

    public TcpGatewayConnection(final IngressDefinition ingress, final int ingressStreamId, final int egressStreamId)
    {
        this.ingress = ingress;
        this.ingressStreamId = ingressStreamId;
        this.egressStreamId = egressStreamId;
    }

    public void connect()
    {
        clientFactory.createConnection(ingress, ingressStreamId, egressStreamId, TcpGateway::new).connect();
    }

    private static class TcpGateway implements ClusterClientApplication
    {
        private final NonBLockingMessageDrivenTransport transport;
        private final IdleStrategy idleStrategy;
        private final AeronCluster aeronCluster;

        public TcpGateway(final AeronCluster aeronCluster, final ClusterClientPublisher publisher)
        {
            this.aeronCluster = aeronCluster;
            final DelineationApplication delineationApplication = new DelineationApplication(new SerializingApplication(new ExpandableArrayBuffer(), 0, publisher::publish));
            this.transport = new NonBLockingMessageDrivenTransport(new NonBlockingTransport(
                    delineationApplication,
                    delineationApplication,
                    new SystemEpochClock(),
                    "networkFacing"
            ));
            idleStrategy = new BackoffIdleStrategy();
        }

        @Override
        public void onMessage(final long clusterSessionId, final long timestamp, final DirectBuffer buffer, final int offset, final int length, final Header header)
        {
            // TODO: check what can be use to multiplex the messages so that only transport messages are passed on
            transport.onSerialized(buffer, offset, length);
        }

        @Override
        public void onSessionEvent(final long correlationId, final long clusterSessionId, final long leadershipTermId, final int leaderMemberId, final EventCode code, final String detail)
        {
        }

        @Override
        public void onStart()
        {
            while (!Thread.currentThread().isInterrupted())
            {
                transport.work();
                idleStrategy.idle(aeronCluster.pollEgress());
            }
        }
    }
}
