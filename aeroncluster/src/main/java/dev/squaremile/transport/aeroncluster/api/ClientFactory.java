package dev.squaremile.transport.aeroncluster.api;

import dev.squaremile.transport.aeroncluster.implementation.ClusterConnection;
import io.aeron.CommonContext;

public class ClientFactory
{
    /**
     * Resolves the aeron directory automatically
     */
    public ClusterConnection createConnection(
            final IngressDefinition ingress,
            final int ingressStreamId,
            final int egressStreamId,
            final ClusterClientApplicationFactory factory
    )
    {
        return new ClusterConnection(ingress, factory, ingressStreamId, egressStreamId, CommonContext.generateRandomDirName());
    }

    public ClusterConnection createConnection(
            final IngressDefinition ingress,
            final int ingressStreamId,
            final int egressStreamId,
            final String aeronDirectory,
            final ClusterClientApplicationFactory factory
    )
    {
        return new ClusterConnection(ingress, factory, ingressStreamId, egressStreamId, aeronDirectory);
    }
}
