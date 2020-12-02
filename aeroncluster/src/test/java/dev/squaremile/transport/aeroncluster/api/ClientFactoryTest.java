package dev.squaremile.transport.aeroncluster.api;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;


import dev.squaremile.transport.aeroncluster.fixtures.ClusterEndpoints;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterNode;
import dev.squaremile.transport.aeroncluster.fixtures.applications.AccumulatorClusteredService;
import dev.squaremile.transport.aeroncluster.fixtures.applications.NumberGeneratorClusterClientApp;
import io.aeron.exceptions.RegistrationException;
import io.github.artsok.RepeatedIfExceptionsTest;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePortPools;
import static dev.squaremile.transport.aeroncluster.fixtures.NodeEndpointsFixture.nodeEndpoints;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

class ClientFactoryTest
{
    @RepeatedIfExceptionsTest(repeats = 2, exceptions = RegistrationException.class)
    void shouldWorkWithAThreeNodeCluster(@TempDir Path tempDir)
    {
        final Map<String, List<Integer>> freePortPools = freePortPools("node0:6", "node1:6", "node2:6", "ingress:3");
        final IngressEndpoints ingressEndpoints = new IngressEndpoints(
                new IngressEndpoints.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)),
                new IngressEndpoints.Endpoint(1, "localhost", freePortPools.get("ingress").get(1)),
                new IngressEndpoints.Endpoint(2, "localhost", freePortPools.get("ingress").get(2))
        );
        final ClusterEndpoints clusterEndpoints = new ClusterEndpoints(
                nodeEndpoints(freePortPools.get("node0"), 0, ingressEndpoints.get(0).endpoint()),
                nodeEndpoints(freePortPools.get("node1"), 1, ingressEndpoints.get(1).endpoint()),
                nodeEndpoints(freePortPools.get("node2"), 2, ingressEndpoints.get(2).endpoint())
        );
        final ClusterNode clusterNode0 = createClusterNode(0, tempDir.resolve("node-0"), clusterEndpoints, new AccumulatorClusteredService());
        final ClusterNode clusterNode1 = createClusterNode(1, tempDir.resolve("node-1"), clusterEndpoints, new AccumulatorClusteredService());
        final ClusterNode clusterNode2 = createClusterNode(2, tempDir.resolve("node-2"), clusterEndpoints, new AccumulatorClusteredService());

        runInBackground(clusterNode0::start);
        runInBackground(clusterNode1::start);
        runInBackground(clusterNode2::start);
        new ClientFactory().createConnection(
                ingressEndpoints,
                (aeronCluster, publisher) -> new NumberGeneratorClusterClientApp(aeronCluster, publisher, 5, 200)
        ).connect();
    }

    @RepeatedIfExceptionsTest(repeats = 2, exceptions = RegistrationException.class)
    void shouldWorkWithASingleNodeCluster(@TempDir Path tempDir)
    {
        final Map<String, List<Integer>> freePortPools = freePortPools("node0:6", "ingress:1");
        runInBackground(createClusterNode(
                0,
                tempDir.resolve("node-0"),
                new ClusterEndpoints(
                        nodeEndpoints(
                                freePortPools.get("node0"),
                                0,
                                new IngressEndpoints.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)).endpoint()
                        )),
                new AccumulatorClusteredService()
        )::start);
        new ClientFactory().createConnection(
                new IngressEndpoints(new IngressEndpoints.Endpoint(0, "localhost", freePortPools.get("ingress").get(0))),
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

    private ClusterNode createClusterNode(final int nodeId, final Path noteTempDir, final ClusterEndpoints clusterEndpoints, final AccumulatorClusteredService clusteredService)
    {
        return ClusterNode.clusterNode(
                noteTempDir.resolve("cluster"),
                noteTempDir.resolve("aeron"),
                nodeId,
                clusterEndpoints,
                clusteredService
        );
    }

}