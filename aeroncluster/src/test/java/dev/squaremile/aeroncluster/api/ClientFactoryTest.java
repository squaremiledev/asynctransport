package dev.squaremile.aeroncluster.api;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;


import dev.squaremile.aeroncluster.support.applications.AccumulatorClusteredService;
import dev.squaremile.aeroncluster.support.applications.NumberGeneratorClusterClientApp;
import dev.squaremile.aeroncluster.support.cluster.ClusterEndpoints;
import dev.squaremile.aeroncluster.support.cluster.ClusterNode;

import static dev.squaremile.aeroncluster.support.cluster.ClusterNode.clusterNode;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePorts;

class ClientFactoryTest
{
    @RepeatedTest(3)
    void shouldConnectAndExchangeMessagesWithTheCluster(@TempDir Path tempDir)
    {
        final List<Integer> ports = freePorts(7);
        final Integer ingressPort = ports.get(0);
        final int nodeId = 0;
        final ClusterNode clusterNode = clusterNode(
                tempDir.resolve("cluster"),
                tempDir.resolve("aeron"),
                nodeId,
                new ClusterEndpoints(
                        new ClusterEndpoints.NodeEndpoints(
                                nodeId,
                                "localhost:" + ingressPort,
                                "localhost:" + ports.get(1),
                                "localhost:" + ports.get(2),
                                "localhost:" + ports.get(3),
                                "localhost:" + ports.get(4),
                                "localhost:" + ports.get(5),
                                "localhost:" + ports.get(6)
                        )
                ),
                new AccumulatorClusteredService()
        );

        Executors.newSingleThreadExecutor().execute(clusterNode::start);

        new ClientFactory().createConnection(
                new IngressEndpoints(new IngressEndpoints.Endpoint(nodeId, "localhost", ingressPort)),
                (aeronCluster, publisher) -> new NumberGeneratorClusterClientApp(aeronCluster, publisher, 5, 200)
        ).connect();
    }
}