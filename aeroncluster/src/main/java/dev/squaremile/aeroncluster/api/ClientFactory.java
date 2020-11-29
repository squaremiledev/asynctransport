package dev.squaremile.aeroncluster.api;

import dev.squaremile.aeroncluster.implementation.ClusterConnection;

public class ClientFactory
{
    public ClusterConnection createConnection(final IngressEndpoints ingressEndpoints, final ClusterClientApplicationFactory factory)
    {
        return new ClusterConnection(ingressEndpoints, factory);
    }
}
