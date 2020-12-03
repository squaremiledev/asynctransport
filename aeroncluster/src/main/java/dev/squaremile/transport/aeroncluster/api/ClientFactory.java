package dev.squaremile.transport.aeroncluster.api;

import dev.squaremile.transport.aeroncluster.implementation.ClusterConnection;
import io.aeron.cluster.client.AeronCluster;

public class ClientFactory
{
    public ClusterConnection createConnection(final IngressDefinition ingress, final ClusterClientApplicationFactory factory)
    {
        return createConnection(ingress, AeronCluster.Configuration.INGRESS_STREAM_ID_DEFAULT, AeronCluster.Configuration.EGRESS_STREAM_ID_DEFAULT, factory);
    }

    public ClusterConnection createConnection(
            final IngressDefinition ingress,
            final int ingressStreamId,
            final int egressStreamId,
            final ClusterClientApplicationFactory factory
    )
    {
        return new ClusterConnection(ingress, factory, ingressStreamId, egressStreamId);
    }
}
