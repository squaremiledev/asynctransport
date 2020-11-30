package dev.squaremile.transport.aeroncluster.api;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;


import dev.squaremile.transport.aeroncluster.fixtures.ClusterEndpoints;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterNode;
import dev.squaremile.transport.aeroncluster.fixtures.applications.AccumulatorClusteredService;
import dev.squaremile.transport.aeroncluster.fixtures.applications.NumberGeneratorClusterClientApp;
import io.aeron.exceptions.RegistrationException;
import io.github.artsok.RepeatedIfExceptionsTest;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePorts;
import static dev.squaremile.transport.aeroncluster.fixtures.NodeEndpointsFixture.nodeEndpoints;

class ClientFactoryTest
{
    public static final int PORTS_NEEDED_FOR_INGRESS = 3;
    public static final int PORTS_NEEDED_BY_CLUSTER = 6;
    private final List<Integer> ports = freePorts(21);
    private final List<Integer> node0FreePorts = ports.subList(PORTS_NEEDED_FOR_INGRESS, PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_CLUSTER);
    private final List<Integer> node1FreePorts = ports.subList(PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_CLUSTER, PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_CLUSTER * 2);
    private final List<Integer> node2FreePorts = ports.subList(PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_CLUSTER * 2, PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_CLUSTER * 3);
    private final IngressEndpoints.Endpoint node0Ingress = new IngressEndpoints.Endpoint(0, "localhost", ports.get(0));
    private final IngressEndpoints.Endpoint node1Ingress = new IngressEndpoints.Endpoint(1, "localhost", ports.get(1));
    private final IngressEndpoints.Endpoint node2Ingress = new IngressEndpoints.Endpoint(2, "localhost", ports.get(2));

    @RepeatedIfExceptionsTest(repeats = 2, exceptions = RegistrationException.class)
    void shouldWorkWithAThreeNodeCluster(@TempDir Path tempDir)
    {
        final IngressEndpoints ingressEndpoints = new IngressEndpoints(node0Ingress, node1Ingress, node2Ingress);
        final ClusterEndpoints clusterEndpoints = new ClusterEndpoints(
                nodeEndpoints(node0FreePorts, 0, ingressEndpoints.get(0).endpoint()),
                nodeEndpoints(node1FreePorts, 1, ingressEndpoints.get(1).endpoint()),
                nodeEndpoints(node2FreePorts, 2, ingressEndpoints.get(2).endpoint())
        );
        final ClusterNode clusterNode0 = createClusterNode(0, tempDir.resolve("node-0"), clusterEndpoints);
        final ClusterNode clusterNode1 = createClusterNode(1, tempDir.resolve("node-1"), clusterEndpoints);
        final ClusterNode clusterNode2 = createClusterNode(2, tempDir.resolve("node-2"), clusterEndpoints);

        Executors.newSingleThreadExecutor().execute(clusterNode0::start);
        Executors.newSingleThreadExecutor().execute(clusterNode1::start);
        Executors.newSingleThreadExecutor().execute(clusterNode2::start);
        new ClientFactory().createConnection(
                ingressEndpoints,
                (aeronCluster, publisher) -> new NumberGeneratorClusterClientApp(aeronCluster, publisher, 5, 200)
        ).connect();
    }

    @RepeatedIfExceptionsTest(repeats = 2, exceptions = RegistrationException.class)
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

    @Disabled
    @RepeatedTest(3)
    void shouldNotHardcodeAnyGlobalResources(@TempDir Path tempDir)
    {
        shouldWorkWithAThreeNodeCluster(tempDir);
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

}