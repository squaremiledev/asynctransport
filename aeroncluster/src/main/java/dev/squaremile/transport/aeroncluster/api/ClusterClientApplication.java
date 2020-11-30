package dev.squaremile.transport.aeroncluster.api;

import io.aeron.cluster.client.EgressListener;

public interface ClusterClientApplication extends EgressListener
{
    void onStart();
}
