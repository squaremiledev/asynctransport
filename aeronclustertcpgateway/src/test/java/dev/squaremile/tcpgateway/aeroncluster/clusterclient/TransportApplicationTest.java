package dev.squaremile.tcpgateway.aeroncluster.clusterclient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.fixtures.ResponseApplication;
import dev.squaremile.asynctcp.fixtures.transport.EventsSpy;
import dev.squaremile.asynctcp.fixtures.transport.network.SampleClient;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.StreamMultiplexClusteredService;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.TcpGatewayClient;
import dev.squaremile.transport.aeroncluster.api.IngressDefinition;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterNode;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.fixtures.transport.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.fixtures.transport.FreePort.freePortPools;
import static dev.squaremile.asynctcp.fixtures.transport.Worker.runUntil;
import static dev.squaremile.tcpgateway.aeroncluster.clusterclient.Assertions.assertEqual;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.endpoints;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.node;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterNode.clusterNode;
import static io.aeron.cluster.client.AeronCluster.Configuration.INGRESS_STREAM_ID_DEFAULT;
import static java.lang.System.arraycopy;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

class TransportApplicationTest
{
    private final SampleClient sampleClient = new SampleClient();
    private final EventsSpy events = EventsSpy.spy();
    private final Map<String, List<Integer>> freePortPools = freePortPools("tcp:1", "ingress:1", "clusterNode:6");
    private final int tcpPort = freePortPools.get("tcp").get(0);

    @Test
    void shouldCreateApplicationThatHandlesMessages(@TempDir Path tempDir) throws IOException
    {
        // Define application
        final TransportApplicationFactory applicationFactory = transport ->
                new ListeningApplication(
                        transport,
                        fixedLengthDelineation(3),
                        tcpPort,
                        events,
                        (connectionTransport, connectionId) -> new ResponseApplication(connectionTransport, System.out::println, value -> (byte)(value * 10))
                );

        // Create cluster with the application
        final ClusterDefinition cluster = new ClusterDefinition(
                node(0, new IngressDefinition.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)), endpoints("localhost", freePortPools.get("clusterNode")))
        );
        final ClusterNode clusterNode = clusterNode(
                cluster.node(0),
                cluster.memberURIs(),
                tempDir.resolve("cluster"),
                tempDir.resolve("aeron"),
                new StreamMultiplexClusteredService(new TcpGatewayClient(1234, applicationFactory))
        );

        // When started cluster and Tcp gateway
        newSingleThreadExecutor().execute(clusterNode::start);
        newSingleThreadExecutor().execute(() -> new TcpGatewayConnection(cluster.ingress(), INGRESS_STREAM_ID_DEFAULT, 1234).connect());

        // Then
        runUntil(() -> events.contains(StartedListening.class));
        assertEqual(events.all(), new StartedListening(tcpPort, 1, fixedLengthDelineation(3)));

        // When
        runUntil(completed(() -> sampleClient.connectedTo(tcpPort)));
        runUntil(() -> events.all().size() >= 2);

        // Then
        assertThat(events.all(ConnectionAccepted.class).get(0).port()).isEqualTo(tcpPort);

        // When
        sampleClient.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        // Then
        assertThat(readReceivedData(9)).isEqualTo(new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90});
    }

    private byte[] readReceivedData(final int expectedDataLength)
    {
        byte[] actualReceivedData = new byte[expectedDataLength];
        runUntil(completed(() -> sampleClient.read(
                actualReceivedData.length,
                actualReceivedData.length,
                (data, length) -> arraycopy(data, 0, actualReceivedData, 0, length)
        )));
        return actualReceivedData;
    }
}
