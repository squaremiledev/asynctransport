package dev.squaremile.transport.aeroncluster.fixtures;

import io.aeron.archive.Archive;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;

public class ClusterContext
{
    private final MediaDriver.Context mediaDriverContext;
    private final Archive.Context archiveContext;
    private final ConsensusModule.Context consensusModuleContext;
    private final ClusteredServiceContainer.Context clusteredServiceContext;

    public ClusterContext(
            final Archive.Context archiveContext,
            final MediaDriver.Context mediaDriverContext,
            final ConsensusModule.Context consensusModuleContext,
            final ClusteredServiceContainer.Context clusteredServiceContext
    )
    {
        this.archiveContext = archiveContext;
        this.mediaDriverContext = mediaDriverContext;
        this.consensusModuleContext = consensusModuleContext;
        this.clusteredServiceContext = clusteredServiceContext;
    }

    public MediaDriver.Context mediaDriverContext()
    {
        return mediaDriverContext;
    }

    public Archive.Context archiveContext()
    {
        return archiveContext;
    }

    public ConsensusModule.Context consensusModuleContext()
    {
        return consensusModuleContext;
    }

    public ClusteredServiceContainer.Context clusteredServiceContext()
    {
        return clusteredServiceContext;
    }
}
