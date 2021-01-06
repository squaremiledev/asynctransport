package dev.squaremile.tcpgateway.aeroncluster.clusterclient;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.fixtures.transport.network.SampleClient;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.StreamMultiplexClusteredService;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.TcpGatewayClient;
import dev.squaremile.transport.aeroncluster.api.ClientFactory;
import dev.squaremile.transport.aeroncluster.api.ClusterClientApplication;
import dev.squaremile.transport.aeroncluster.api.IngressDefinition;
import dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition;
import io.aeron.logbuffer.Header;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.fixtures.transport.FreePort.freePortPools;
import static dev.squaremile.asynctcp.fixtures.transport.Worker.noExceptionAnd;
import static dev.squaremile.asynctcp.fixtures.transport.Worker.runUntil;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.endpoints;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterDefinition.node;
import static dev.squaremile.transport.aeroncluster.fixtures.ClusterNode.clusterNode;
import static io.aeron.cluster.client.AeronCluster.Configuration.INGRESS_STREAM_ID_DEFAULT;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

class TcpGatewayConnectionTest
{
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();

    @Test
    void shouldDetectTcpGatewayClientsByStreamId(@TempDir Path tempDir)
    {
        final int tcpGatewayEgressStreamId = new Random().nextInt();
        final int anotherTcpGatewayEgressStreamId = tcpGatewayEgressStreamId + 1;
        final int nonTcpClientStreamId = tcpGatewayEgressStreamId + 2;
        final AtomicBoolean receivedMessageOnNonTcpGatewayClient = new AtomicBoolean(false);
        final Map<String, List<Integer>> freePortPools = freePortPools("tcp:2", "ingress:1", "clusterNode:6");
        final ClusterDefinition cluster = new ClusterDefinition(
                node(0, new IngressDefinition.Endpoint(0, "localhost", freePortPools.get("ingress").get(0)), endpoints("localhost", freePortPools.get("clusterNode")))
        );
        newSingleThreadExecutor().execute(clusterNode(
                cluster.node(0),
                cluster.memberURIs(),
                tempDir.resolve("cluster"),
                tempDir.resolve("aeron"),
                new StreamMultiplexClusteredService(
                        new TcpGatewayClient(tcpGatewayEgressStreamId, transport -> new TransportApplicationOnDuty()
                        {
                            @Override
                            public void onStart()
                            {
                                transport.handle(transport.command(Listen.class).set(1, freePortPools.get("tcp").get(0), fixedLengthDelineation(1)));
                            }

                            @Override
                            public void onEvent(final Event event)
                            {
                                System.out.println(event);
                            }
                        }),
                        new TcpGatewayClient(anotherTcpGatewayEgressStreamId, transport -> new TransportApplicationOnDuty()
                        {
                            @Override
                            public void onStart()
                            {
                                transport.handle(transport.command(Listen.class).set(1, freePortPools.get("tcp").get(1), fixedLengthDelineation(1)));
                            }

                            @Override
                            public void onEvent(final Event event)
                            {
                                System.out.println(event);
                            }
                        })
                )
        )::start);

        // When
        newSingleThreadExecutor().execute(() -> new ClientFactory().createConnection(
                cluster.ingress(),
                INGRESS_STREAM_ID_DEFAULT,
                nonTcpClientStreamId,
                (aeronCluster, publisher) -> new ClusterClientApplication()
                {
                    @Override
                    public void onStart()
                    {
                        while (!Thread.currentThread().isInterrupted())
                        {
                            idleStrategy.idle(aeronCluster.pollEgress());
                        }
                    }

                    @Override
                    public void onMessage(final long clusterSessionId, final long timestamp, final DirectBuffer buffer, final int offset, final int length, final Header header)
                    {
                        receivedMessageOnNonTcpGatewayClient.set(true);
                    }
                }
        ).connect());
        newSingleThreadExecutor().execute(() -> new TcpGatewayConnection(cluster.ingress(), INGRESS_STREAM_ID_DEFAULT, tcpGatewayEgressStreamId).connect());
        newSingleThreadExecutor().execute(() -> new TcpGatewayConnection(cluster.ingress(), INGRESS_STREAM_ID_DEFAULT, anotherTcpGatewayEgressStreamId).connect());

        // Then
        runUntil(noExceptionAnd(() -> new SampleClient().connectedTo(freePortPools.get("tcp").get(0)) != null));
        runUntil(noExceptionAnd(() -> new SampleClient().connectedTo(freePortPools.get("tcp").get(1)) != null));
        assertThat(receivedMessageOnNonTcpGatewayClient.get()).describedAs("Does not expect any messages here, it's not a tcp gateway after all").isFalse();
    }
}