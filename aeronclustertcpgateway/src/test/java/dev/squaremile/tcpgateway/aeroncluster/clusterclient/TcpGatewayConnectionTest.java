package dev.squaremile.tcpgateway.aeroncluster.clusterclient;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.testfixtures.network.SampleClient;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.TcpHandlingClusteredService;
import dev.squaremile.transport.aeroncluster.api.IngressEndpoints;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterEndpoints;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePorts;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.noExceptionAnd;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterNode.clusterNode;
import static dev.squaremile.transport.aeroncluster.fixtures.NodeEndpointsFixture.nodeEndpoints;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

class TcpGatewayConnectionTest
{
    public static final int PORTS_NEEDED_FOR_INGRESS = 3;
    public static final int PORTS_NEEDED_BY_CLUSTER = 6;
    public static final int PORTS_NEEDED_BY_TCP = 1;

    private final List<Integer> freePortPool = freePorts(10);
    private final List<Integer> nodeFreePortPool = freePortPool.subList(PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_TCP, PORTS_NEEDED_FOR_INGRESS + PORTS_NEEDED_BY_TCP + PORTS_NEEDED_BY_CLUSTER);
    private final int tcpPort = freePortPool.get(1);
    private final IngressEndpoints ingressEndpoints = new IngressEndpoints(new IngressEndpoints.Endpoint(0, "localhost", freePortPool.get(0)));
    private final ClusterEndpoints clusterEndpoints = new ClusterEndpoints(nodeEndpoints(nodeFreePortPool, 0, ingressEndpoints.get(0).endpoint()));

    @Test
    void shouldUseTcpGatewayToInteractWithTcpLayerFromWithinTheCluster(@TempDir Path tempDir)
    {
        newSingleThreadExecutor().execute(clusterNode(
                tempDir.resolve("cluster"),
                tempDir.resolve("aeron"),
                0,
                clusterEndpoints,
                new TcpHandlingClusteredService(
                        transport -> transport.handle(transport.command(Listen.class).set(1, tcpPort, fixedLengthDelineation(1))),
                        System.out::println
                )
        )::start);
        newSingleThreadExecutor().execute(() -> new TcpGatewayConnection(ingressEndpoints).connect());

        // When
        runUntil(noExceptionAnd(() -> new SampleClient().connectedTo(tcpPort) != null));
        runUntil(noExceptionAnd(() -> new SampleClient().connectedTo(tcpPort) != null));
        runUntil(noExceptionAnd(() -> new SampleClient().connectedTo(tcpPort) != null));
    }
}