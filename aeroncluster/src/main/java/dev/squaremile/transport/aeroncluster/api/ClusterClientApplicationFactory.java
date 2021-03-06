package dev.squaremile.transport.aeroncluster.api;

import io.aeron.cluster.client.AeronCluster;

public interface ClusterClientApplicationFactory
{
    ClusterClientApplication create(AeronCluster aeronCluster, ClusterClientPublisher clusterClientPublisher);
}
