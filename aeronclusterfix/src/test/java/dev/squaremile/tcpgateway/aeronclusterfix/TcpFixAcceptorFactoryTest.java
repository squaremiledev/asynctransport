package dev.squaremile.tcpgateway.aeronclusterfix;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.fix.FixHandler;
import dev.squaremile.asynctcp.fix.examplecertification.usecases.RespondToLogOnIgnoreRest;
import dev.squaremile.asynctcp.fixtures.transport.EventsSpy;
import dev.squaremile.asynctcp.fixtures.transport.network.SampleClient;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.StreamMultiplexClusteredService;
import dev.squaremile.transport.aeroncluster.api.IngressDefinition;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterNode;

import static dev.squaremile.asynctcp.fix.examplecertification.usecases.FixUtils.asciiFixBody;
import static dev.squaremile.asynctcp.fixtures.transport.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.support.transport.FreePort.freePortPools;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;
import static dev.squaremile.tcpgateway.aeronclusterfix.TcpFixAcceptorFactory.createClusteredTcpFixAcceptor;
import static dev.squaremile.tcpgateway.aeronclusterfix.TcpFixAcceptorFactory.createTcpGateway;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.endpoints;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.node;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterNode.clusterNode;
import static io.aeron.cluster.client.AeronCluster.Configuration.INGRESS_STREAM_ID_DEFAULT;
import static java.lang.System.arraycopy;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class TcpFixAcceptorFactoryTest
{
    private final SampleClient sampleClient = new SampleClient();
    private final EventsSpy events = EventsSpy.spy();
    private final Map<String, List<Integer>> freePortPools = freePortPools("tcp:1", "ingress:1", "clusterNode:6");
    private final int tcpPort = freePortPools.get("tcp").get(0);
    private final byte[] logonMessage = asciiFixBody(
            "FIXT.1.1", "35=A^49=BuySide^56=SellSide^34=1^52=20190605-11:51:27.848^1128=9^98=0^108=30^141=Y^553=Username^554=Password^1137=9^"
    );

    @Test
        // TODO: identify the source of an infrequent failure when run by the Github Actions
    void shouldRunApplication(@TempDir Path tempDir) throws IOException
    {
        final FixHandler fixHandler = new RespondToLogOnIgnoreRest("someUsername");
        final ClusterDefinition cluster = new ClusterDefinition(
                node(0, new IngressDefinition.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)), endpoints("localhost", freePortPools.get("clusterNode")))
        );
        final ClusterNode clusterNode = clusterNode(
                cluster.node(0),
                cluster.memberURIs(),
                tempDir.resolve("cluster"),
                tempDir.resolve("aeron"),
                new StreamMultiplexClusteredService(createClusteredTcpFixAcceptor(1234, tcpPort, events, fixHandler))
        );

        newSingleThreadExecutor().execute(clusterNode::start);
        newSingleThreadExecutor().execute(() -> createTcpGateway(INGRESS_STREAM_ID_DEFAULT, 1234, cluster.ingress(), tempDir.resolve("aeron_client").toString()).connect());
        runUntil(() -> events.contains(StartedListening.class));
        runUntil(completed(() -> sampleClient.connectedTo(tcpPort)));

        // When
        sampleClient.write(logonMessage);

        // Then
        assertThat(readReceivedData(logonMessage.length)).contains("someUsername");
    }

    private String readReceivedData(final int expectedDataLength)
    {
        byte[] actualReceivedData = new byte[expectedDataLength];
        runUntil(completed(() -> sampleClient.read(
                actualReceivedData.length,
                actualReceivedData.length,
                (data, length) -> arraycopy(data, 0, actualReceivedData, 0, length)
        )));
        return new String(actualReceivedData);
    }

}
