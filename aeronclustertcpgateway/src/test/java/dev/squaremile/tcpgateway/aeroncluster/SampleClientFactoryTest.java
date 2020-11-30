package dev.squaremile.tcpgateway.aeroncluster;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import dev.squaremile.transport.aeroncluster.api.ClientFactory;
import dev.squaremile.transport.aeroncluster.api.IngressEndpoints;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterEndpoints;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterNode;
import dev.squaremile.transport.aeroncluster.fixtures.applications.AccumulatorClusteredService;
import dev.squaremile.transport.aeroncluster.fixtures.applications.NumberGeneratorClusterClientApp;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePorts;

class SampleClientFactoryTest
{
    public static final int PORTS_NEEDED_FOR_INGRESS = 3;
    public static final int PORTS_NEEDED_BY_CLUSTER = 6;
    private final List<Integer> ports = freePorts(21);
    private final List<Integer> node0FreePorts = ports.subList(PORTS_NEEDED_FOR_INGRESS, PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_CLUSTER);
    private final IngressEndpoints.Endpoint node0Ingress = new IngressEndpoints.Endpoint(0, "localhost", ports.get(0));

    @Test
    void shouldWorkWithASingleNodeCluster(@TempDir Path tempDir)
    {
        final IngressEndpoints ingressEndpoints = new IngressEndpoints(node0Ingress);
        final ClusterEndpoints clusterEndpoints = new ClusterEndpoints(
                nodeEndpoints(node0FreePorts, 0, ingressEndpoints.get(0).endpoint())
        );
        final ClusterNode clusterNode0 = createClusterNode(0, tempDir.resolve("node-0"), clusterEndpoints);
        Executors.newSingleThreadExecutor().execute(clusterNode0::start);

        new ClientFactory().createConnection(
                ingressEndpoints,
                (aeronCluster, publisher) -> new NumberGeneratorClusterClientApp(aeronCluster, publisher, 5, 200)
        ).connect();
    }

    private ClusterNode createClusterNode(final int nodeId, final Path noteTempDir, final ClusterEndpoints clusterEndpoints)
    {
        return ClusterNode.clusterNode(
                noteTempDir.resolve("cluster"),
                noteTempDir.resolve("aeron"),
                nodeId,
                clusterEndpoints,
                new AccumulatorClusteredService()
        );
    }

    private ClusterEndpoints.NodeEndpoints nodeEndpoints(final List<Integer> nodeFreePorts, final int nodeId, final String ingress)
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