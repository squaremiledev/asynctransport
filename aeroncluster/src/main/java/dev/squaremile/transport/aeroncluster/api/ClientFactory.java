package dev.squaremile.transport.aeroncluster.api;

import dev.squaremile.transport.aeroncluster.implementation.ClusterConnection;

public class ClientFactory
{
    public ClusterConnection createConnection(final IngressDefinition ingress, final ClusterClientApplicationFactory factory)
    {
        return new ClusterConnection(ingress, factory);
    }
}
