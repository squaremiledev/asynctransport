package dev.squaremile.transport.aeroncluster.fixtures;

import java.nio.file.Path;

import org.agrona.concurrent.NoOpLock;
import org.agrona.concurrent.ShutdownSignalBarrier;


import io.aeron.ChannelUriStringBuilder;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.client.AeronArchive;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class ClusterNode
{
    private final ClusterContext clusterContext;
    private final ShutdownSignalBarrier shutdownSignalBarrier;

    private ClusterNode(final ClusterContext clusterContext, final ShutdownSignalBarrier shutdownSignalBarrier)
    {
        this.clusterContext = clusterContext;
        this.shutdownSignalBarrier = shutdownSignalBarrier;
    }

    public static ClusterNode clusterNode(
            final ClusterDefinition.NodeDefinition node,
            final String memberURIs,
            final Path clusterDirectory,
            final Path aeronDirectory,
            final ClusteredService clusteredService
    )
    {
        final TermLength termLength = new TermLength(64);
        final Archive.Context archiveContext = new Archive.Context()
                .threadingMode(ArchiveThreadingMode.SHARED)
                .aeronDirectoryName(aeronDirectory.toString())
                .archiveDir(clusterDirectory.resolve("archiveDir").toFile())
                .controlChannel(
                        new ChannelUriStringBuilder()
                                .media("udp")
                                .termLength(termLength.asBytes())
                                .endpoint(node.archiveControl())
                                .build()
                )
                .recordingEventsChannel("aeron:udp?control-mode=dynamic|control=" + node.recordingEvents())
                .localControlChannel("aeron:ipc?term-length=" + termLength.asChannelParameter());
        final ShutdownSignalBarrier shutdownSignalBarrier = new ShutdownSignalBarrier();
        final ClusterContext clusterContext = new ClusterContext(
                archiveContext,
                new MediaDriver.Context()
                        .threadingMode(ThreadingMode.SHARED)
                        .termBufferSparseFile(true)
                        .aeronDirectoryName(aeronDirectory.toString())
                        .terminationHook(shutdownSignalBarrier::signal),
                new ConsensusModule.Context()
                        .clusterMemberId(node.nodeId())
                        .clusterMembers(memberURIs)
                        .clusterDir(clusterDirectory.resolve("consensusDir").toFile())
                        //.replicationChannel("aeron:udp?endpoint=localhost:0")
                        .ingressChannel("aeron:udp?term-length=" + termLength.asChannelParameter())
                        .logChannel(
                                new ChannelUriStringBuilder()
                                        .media("udp")
                                        .termLength(termLength.asBytes())
                                        .controlMode("manual")
                                        .controlEndpoint(node.logControl())
                                        .build()
                        )
                        .archiveContext(
                                new AeronArchive.Context()
                                        .lock(NoOpLock.INSTANCE)
                                        .controlRequestChannel(archiveContext.localControlChannel())
                                        .controlRequestStreamId(archiveContext.localControlStreamId())
                                        .recordingEventsChannel(archiveContext.recordingEventsChannel())
                                        .controlResponseChannel(archiveContext.localControlChannel() + "|term-length=" + termLength.asChannelParameter())
                                        .aeronDirectoryName(aeronDirectory.toString()).clone()
                        ),
                new ClusteredServiceContainer.Context()
                        .clusteredService(clusteredService)
                        .aeronDirectoryName(aeronDirectory.toString())
                        .archiveContext(
                                new AeronArchive.Context()
                                        .lock(NoOpLock.INSTANCE)
                                        .controlRequestChannel(archiveContext.localControlChannel())
                                        .controlRequestStreamId(archiveContext.localControlStreamId())
                                        .controlResponseChannel(archiveContext.localControlChannel() + "|term-length=" + termLength.asChannelParameter())
                                        .aeronDirectoryName(aeronDirectory.toString()).clone()
                        )
                        .clusterDir(clusterDirectory.resolve("clusterDir").toFile())
        );
        return new ClusterNode(clusterContext, shutdownSignalBarrier);
    }

    public void start()
    {
        try (
                ClusteredMediaDriver clusteredMediaDriver = ClusteredMediaDriver.launch(
                        clusterContext.mediaDriverContext(), clusterContext.archiveContext(), clusterContext.consensusModuleContext()
                );
                ClusteredServiceContainer clusteredServiceContainer = ClusteredServiceContainer.launch(clusterContext.clusteredServiceContext())
        )
        {
            shutdownSignalBarrier.await();
        }
    }
}
