package dev.squaremile.aeroncluster.api;

import io.aeron.cluster.client.EgressListener;

public interface ClusterClientApplication extends EgressListener
{
    void onStart();
}
