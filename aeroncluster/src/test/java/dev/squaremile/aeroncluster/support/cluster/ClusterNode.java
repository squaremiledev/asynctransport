package dev.squaremile.aeroncluster.support.cluster;

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
    private final Runnable onReady;

    public ClusterNode(final ClusterContext clusterContext, final Runnable onReady)
    {
        this.clusterContext = clusterContext;
        this.onReady = onReady;
    }

    public static ClusterNode clusterNode(
            final Path clusterDirectory,
            final Path aeronDirectory,
            final int nodeId,
            final ClusterEndpoints clusterEndpoints,
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
                                .endpoint(clusterEndpoints.node(nodeId).archiveControl())
                                .build()
                )
                .recordingEventsChannel("aeron:udp?control-mode=dynamic|control=" + clusterEndpoints.node(nodeId).recordingEvents())
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
                        .clusterMemberId(nodeId)
                        .clusterMembers(clusterEndpoints.asUri())
                        .clusterDir(clusterDirectory.resolve("consensusDir").toFile())
                        .ingressChannel("aeron:udp?term-length=" + termLength.asChannelParameter())
                        .logChannel(
                                new ChannelUriStringBuilder()
                                        .media("udp")
                                        .termLength(termLength.asBytes())
                                        .controlMode("manual")
                                        .controlEndpoint(clusterEndpoints.node(nodeId).logControl())
                                        .build()
                        )
                        .archiveContext(
                                new AeronArchive.Context()
                                        .lock(NoOpLock.INSTANCE)
                                        .controlRequestChannel(archiveContext.controlChannel())
                                        .controlRequestStreamId(archiveContext.controlStreamId())
                                        .recordingEventsChannel(archiveContext.recordingEventsChannel())
                                        .controlResponseChannel("aeron:udp?endpoint=localhost:0|term-length=" + termLength.asChannelParameter())
                                        .aeronDirectoryName(aeronDirectory.toString()).clone()
                        ),
                new ClusteredServiceContainer.Context()
                        .clusteredService(clusteredService)
                        .aeronDirectoryName(aeronDirectory.toString())
                        .archiveContext(
                                new AeronArchive.Context()
                                        .lock(NoOpLock.INSTANCE)
                                        .controlRequestChannel(archiveContext.controlChannel())
                                        .controlRequestStreamId(archiveContext.controlStreamId())
                                        .controlResponseChannel("aeron:udp?endpoint=localhost:0|term-length=" + termLength.asChannelParameter())
                                        .aeronDirectoryName(aeronDirectory.toString()).clone()
                        )
                        .clusterDir(clusterDirectory.resolve("clusterDir").toFile())
        );
        return new ClusterNode(clusterContext, shutdownSignalBarrier::await);
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
            onReady.run();
        }
    }
}
