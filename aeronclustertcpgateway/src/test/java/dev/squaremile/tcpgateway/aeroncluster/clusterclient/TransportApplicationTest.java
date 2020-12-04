package dev.squaremile.tcpgateway.aeroncluster.clusterclient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.fixtures.ResponseApplication;
import dev.squaremile.asynctcp.fixtures.TimingExtension;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.testfixtures.EventsSpy;
import dev.squaremile.asynctcp.transport.testfixtures.network.SampleClient;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.StreamMultiplexClusteredService;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.TcpGatewayClient;
import dev.squaremile.transport.aeroncluster.api.IngressDefinition;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.transport.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePortPools;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.endpoints;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.node;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterNode.clusterNode;
import static io.aeron.cluster.client.AeronCluster.Configuration.INGRESS_STREAM_ID_DEFAULT;
import static java.lang.System.arraycopy;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

@ExtendWith(TimingExtension.class)
class TransportApplicationTest
{
    private final SampleClient sampleClient = new SampleClient();
    private final EventsSpy events = EventsSpy.spy();
    private final Map<String, List<Integer>> freePortPools = freePortPools("tcp:1", "ingress:1", "clusterNode:6");
    private final int tcpPort = freePortPools.get("tcp").get(0);

    @Test
    void shouldCreateApplicationThatHandlesMessages(@TempDir Path tempDir) throws IOException
    {
        final ClusterDefinition cluster = new ClusterDefinition(
                node(0, new IngressDefinition.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)), endpoints("localhost", freePortPools.get("clusterNode")))
        );

        newSingleThreadExecutor().execute(clusterNode(
                cluster.node(0),
                cluster.memberURIs(),
                tempDir.resolve("cluster"),
                tempDir.resolve("aeron"),
                new StreamMultiplexClusteredService(
                        new TcpGatewayClient(
                                1234,
                                transport ->
                                        new ListeningApplication(
                                                transport,
                                                fixedLengthDelineation(3),
                                                tcpPort,
                                                events,
                                                (connectionTransport, connectionId) -> new ResponseApplication(
                                                        connectionTransport,
                                                        System.out::println,
                                                        value -> (byte)(value * 10)
                                                )
                                        )
                        )
                )
        )::start);
        newSingleThreadExecutor().execute(() -> new TcpGatewayConnection(cluster.ingress(), INGRESS_STREAM_ID_DEFAULT, 1234).connect());

        // Given
        runUntil(() -> events.contains(StartedListening.class));
        assertEqual(events.all(), new StartedListening(tcpPort, 1, fixedLengthDelineation(3)));

        // When
        runUntil(completed(() -> sampleClient.connectedTo(tcpPort)));
        runUntil(() -> events.all().size() >= 2);

        // Then
        assertThat(events.all(ConnectionAccepted.class).get(0).port()).isEqualTo(tcpPort);

        // DATA SENDING PART
        byte[] dataToSend = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] expectedReceivedData = new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90};

        // When
        sampleClient.write(dataToSend);

        // Then
        assertThat(readReceivedData(dataToSend.length)).isEqualTo(expectedReceivedData);
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
