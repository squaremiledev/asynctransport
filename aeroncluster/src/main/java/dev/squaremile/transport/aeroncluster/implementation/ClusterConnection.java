package dev.squaremile.transport.aeroncluster.implementation;

import java.util.function.Consumer;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;


import dev.squaremile.transport.aeroncluster.api.ClusterClientApplication;
import dev.squaremile.transport.aeroncluster.api.ClusterClientApplicationFactory;
import dev.squaremile.transport.aeroncluster.api.ClusterClientPublisher;
import dev.squaremile.transport.aeroncluster.api.IngressDefinition;
import io.aeron.CommonContext;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;

public class ClusterConnection
{
    private final ApplicationWrapper applicationWrapper;
    private final EmbeddedClusterClient embeddedClusterClient;

    public ClusterConnection(final IngressDefinition ingress, final ClusterClientApplicationFactory factory, final int ingressStreamId, final int egressStreamId)
    {
        this.applicationWrapper = new ApplicationWrapper(factory);
        this.embeddedClusterClient = new EmbeddedClusterClient(new ClusterClientContext(ingress, applicationWrapper, ingressStreamId, egressStreamId));
    }

    public void connect()
    {
        embeddedClusterClient.connect(applicationWrapper::onStart);
    }

    private static class EmbeddedClusterClient
    {
        private final ClusterClientContext context;

        public EmbeddedClusterClient(final ClusterClientContext context)
        {
            this.context = context;
        }

        public void connect(Consumer<AeronCluster> readyAeronCluster)
        {
            try (
                    MediaDriver mediaDriver = MediaDriver.launchEmbedded(context.buildMediaDriverContext());
                    AeronCluster aeronCluster = AeronCluster.connect(context.buildAeronClusterContext())
            )
            {
                readyAeronCluster.accept(aeronCluster);
            }
        }
    }

    private static class ApplicationWrapper implements EgressListener
    {
        private final ClusterClientApplicationFactory applicationFactory;
        private ClusterClientApplication application;

        public ApplicationWrapper(final ClusterClientApplicationFactory applicationFactory)
        {
            this.applicationFactory = applicationFactory;
        }

        @Override
        public void onMessage(final long clusterSessionId, final long timestamp, final DirectBuffer buffer, final int offset, final int length, final Header header)
        {
            if (application != null)
            {
                application.onMessage(clusterSessionId, timestamp, buffer, offset, length, header);
            }
        }

        @Override
        public void onSessionEvent(final long correlationId, final long clusterSessionId, final long leadershipTermId, final int leaderMemberId, final EventCode code, final String detail)
        {
            if (application != null)
            {
                application.onSessionEvent(correlationId, clusterSessionId, leadershipTermId, leaderMemberId, code, detail);
            }
        }

        @Override
        public void onNewLeader(final long clusterSessionId, final long leadershipTermId, final int leaderMemberId, final String ingressEndpoints)
        {
            if (application != null)
            {
                application.onNewLeader(clusterSessionId, leadershipTermId, leaderMemberId, ingressEndpoints);
            }
        }

        public void onStart(final AeronCluster aeronCluster)
        {
            application = applicationFactory.create(aeronCluster, new FullyPublishingClusterClientPublisher(aeronCluster));
            application.onStart();
        }

    }

    private static class ClusterClientContext
    {
        private final MediaDriverContext mediaDriverContext;
        private final AeronClusterContext aeronClusterContext;

        public ClusterClientContext(IngressDefinition ingress, EgressListener egressListener, final int ingressStreamId, final int egressStreamId)
        {
            this.mediaDriverContext = new MediaDriverContext();
            this.aeronClusterContext = new AeronClusterContext(
                    mediaDriverContext.aeronDirectory(),
                    ingress,
                    ingressStreamId,
                    egressListener,
                    egressStreamId
            );
        }

        public AeronCluster.Context buildAeronClusterContext()
        {
            return aeronClusterContext.buildContext();
        }

        public MediaDriver.Context buildMediaDriverContext()
        {
            return mediaDriverContext.buildContext();
        }
    }

    private static class AeronClusterContext
    {
        private final EgressListener egressListener;
        private final String aeronDirectory;
        private final IngressDefinition ingress;
        private final int ingressStreamId;
        private final int egressStreamId;

        public AeronClusterContext(
                final String aeronDirectory,
                final IngressDefinition ingress,
                final int ingressStreamId,
                final EgressListener egressListener,
                final int egressStreamId
        )
        {
            this.egressListener = egressListener;
            this.aeronDirectory = aeronDirectory;
            this.ingress = ingress;
            this.ingressStreamId = ingressStreamId;
            this.egressStreamId = egressStreamId;
        }

        public AeronCluster.Context buildContext()
        {
            return new AeronCluster.Context()
                    .ingressChannel("aeron:udp")
                    .egressChannel("aeron:udp?endpoint=localhost:0")
                    .ingressStreamId(ingressStreamId)
                    .egressStreamId(egressStreamId)
                    .aeronDirectoryName(aeronDirectory)
                    .ingressEndpoints(ingress.asUri())
                    .egressListener(egressListener);
        }
    }

    private static class MediaDriverContext
    {
        private final String aeronDirectory;

        public MediaDriverContext()
        {
            aeronDirectory = CommonContext.generateRandomDirName();
        }

        public String aeronDirectory()
        {
            return aeronDirectory;
        }

        public MediaDriver.Context buildContext()
        {
            return new MediaDriver.Context()
                    .dirDeleteOnStart(true)
                    .dirDeleteOnShutdown(true)
                    .threadingMode(ThreadingMode.SHARED)
                    .aeronDirectoryName(aeronDirectory);
        }
    }

    public static class FullyPublishingClusterClientPublisher implements ClusterClientPublisher
    {
        private final AeronCluster aeronCluster;
        private final IdleStrategy idleStrategy = new BackoffIdleStrategy();

        public FullyPublishingClusterClientPublisher(final AeronCluster aeronCluster)
        {
            this.aeronCluster = aeronCluster;
        }

        @Override
        public void publish(final DirectBuffer buffer, final int offset, final int length)
        {
            idleStrategy.reset();
            while (this.aeronCluster.offer(buffer, offset, length) < 0)
            {
                idleStrategy.idle(aeronCluster.pollEgress());
            }
        }
    }
}
