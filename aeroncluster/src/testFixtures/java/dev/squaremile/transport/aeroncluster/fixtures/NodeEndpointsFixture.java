package dev.squaremile.transport.aeroncluster.fixtures;

import java.util.List;

public class NodeEndpointsFixture
{
    public static ClusterEndpoints.NodeEndpoints nodeEndpoints(final List<Integer> nodeFreePorts, final int nodeId, final String ingress)
    {
        return new ClusterEndpoints.NodeEndpoints(
                nodeId,
                ingress,
                "localhost:" + nodeFreePorts.get(0),
                "localhost:" + nodeFreePorts.get(1),
                "localhost:" + nodeFreePorts.get(2),
                "localhost:" + nodeFreePorts.get(3),
                "localhost:" + nodeFreePorts.get(4),
                "localhost:" + nodeFreePorts.get(5)
        );
    }
}
