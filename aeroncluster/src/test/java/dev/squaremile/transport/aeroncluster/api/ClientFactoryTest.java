package dev.squaremile.transport.aeroncluster.api;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;


import dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterNode;
import dev.squaremile.transport.aeroncluster.fixtures.applications.AccumulatorClusteredService;
import dev.squaremile.transport.aeroncluster.fixtures.applications.NumberGeneratorClusterClientApp;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.exceptions.RegistrationException;
import io.github.artsok.RepeatedIfExceptionsTest;

import static dev.squaremile.asynctcp.support.transport.FreePort.freePortPools;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.endpoints;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.node;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

class ClientFactoryTest
{
    @RepeatedIfExceptionsTest(repeats = 2, exceptions = RegistrationException.class)
    void shouldWorkWithAThreeNodeCluster(@TempDir Path tempDir)
    {
        final Map<String, List<Integer>> freePortPools = freePortPools("node0:6", "node1:6", "node2:6", "ingress:3");
        final ClusterDefinition cluster = new ClusterDefinition(
                node(0, new IngressDefinition.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)), endpoints("localhost", freePortPools.get("node0"))),
                node(1, new IngressDefinition.Endpoint(1, "localhost", freePortPools.get("ingress").get(1)), endpoints("localhost", freePortPools.get("node1"))),
                node(2, new IngressDefinition.Endpoint(2, "localhost", freePortPools.get("ingress").get(2)), endpoints("localhost", freePortPools.get("node2")))
        );

        runInBackground(ClusterNode.clusterNode(
                cluster.node(0),
                cluster.memberURIs(),
                tempDir.resolve("node-0").resolve("cluster"),
                tempDir.resolve("node-0").resolve("aeron"),
                new AccumulatorClusteredService()
        )::start);
        runInBackground(ClusterNode.clusterNode(
                cluster.node(1),
                cluster.memberURIs(),
                tempDir.resolve("node-1").resolve("cluster"),
                tempDir.resolve("node-1").resolve("aeron"),
                new AccumulatorClusteredService()
        )::start);
        runInBackground(ClusterNode.clusterNode(
                cluster.node(2),
                cluster.memberURIs(),
                tempDir.resolve("node-2").resolve("cluster"),
                tempDir.resolve("node-2").resolve("aeron"),
                new AccumulatorClusteredService()
        )::start);

        new ClientFactory().createConnection(
                cluster.ingress(),
                AeronCluster.Configuration.INGRESS_STREAM_ID_DEFAULT,
                AeronCluster.Configuration.EGRESS_STREAM_ID_DEFAULT,
                tempDir.resolve("aeron_client").toString(),
                (aeronCluster, publisher) -> new NumberGeneratorClusterClientApp(aeronCluster, publisher, 5, 200)
        ).connect();
    }

    @RepeatedIfExceptionsTest(repeats = 2, exceptions = RegistrationException.class)
    void shouldWorkWithASingleNodeCluster(@TempDir Path tempDir)
    {
        final Map<String, List<Integer>> freePortPools = freePortPools("node0:6", "ingress:1");
        final ClusterDefinition cluster = new ClusterDefinition(
                node(0, new IngressDefinition.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)), endpoints("localhost", freePortPools.get("node0")))
        );
        final Path noteTempDir = tempDir.resolve("node-0");
        runInBackground(ClusterNode.clusterNode(
                cluster.node(0),
                cluster.memberURIs(),
                noteTempDir.resolve("cluster"),
                noteTempDir.resolve("aeron"),
                new AccumulatorClusteredService()
        )::start);
        new ClientFactory().createConnection(
                cluster.ingress(),
                AeronCluster.Configuration.INGRESS_STREAM_ID_DEFAULT,
                AeronCluster.Configuration.EGRESS_STREAM_ID_DEFAULT,
                tempDir.resolve("aeron_client").toString(),
                (aeronCluster, publisher) -> new NumberGeneratorClusterClientApp(aeronCluster, publisher, 5, 200)
        ).connect();
    }

    @Disabled
    @RepeatedTest(3)
    void shouldNotHardcodeAnyGlobalResources(@TempDir Path tempDir)
    {
        shouldWorkWithAThreeNodeCluster(tempDir);
    }

    private void runInBackground(final Runnable task)
    {
        newSingleThreadExecutor().execute(task);
    }
}