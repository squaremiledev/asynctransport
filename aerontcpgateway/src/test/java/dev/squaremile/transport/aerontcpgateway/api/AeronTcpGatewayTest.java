package dev.squaremile.transport.aerontcpgateway.api;

import org.agrona.collections.MutableBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.support.transport.FreePort;
import dev.squaremile.asynctcp.support.transport.ThingsOnDutyRunner;
import dev.squaremile.transport.aerontcpgateway.FakeServer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;

class AeronTcpGatewayTest
{
    private final int port = FreePort.freePort();
    private MediaDriver mediaDriver;
    private TransportApplicationOnDuty fakeServer;
    private AeronTcpGateway gateway;
    private AeronTcpGatewayClient gatewayClient;
    private ThingsOnDutyRunner runner;

    @BeforeEach
    void setUp()
    {
        mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED).dirDeleteOnShutdown(true));
        fakeServer = FakeServer.startFakeServerListeningOn(port);
        gateway = new AeronTcpGateway(new AeronConnection(10, 11, mediaDriver.aeronDirectoryName()));
        gatewayClient = new AeronTcpGatewayClient(new AeronConnection(10, 11, mediaDriver.aeronDirectoryName()));
        runner = new ThingsOnDutyRunner(fakeServer, gatewayClient, gateway);
    }

    @AfterEach
    void tearDown()
    {
        gatewayClient.close();
        gateway.close();
        fakeServer.close();
        mediaDriver.close();
    }

    @Test
    void shouldConnectToTheGateway()
    {
        assertThat(gateway.isConnected()).isFalse();
        assertThat(gatewayClient.isConnected()).isFalse();

        gateway.connect();
        gatewayClient.connect();
        runUntil(runner.reached(() -> gatewayClient.isConnected() && gateway.isConnected()));

        assertThat(gateway.isConnected()).isTrue();
        assertThat(gatewayClient.isConnected()).isTrue();
    }

    @Test
    void shouldNotAllowToStartApplicationUntilGatewayIsConnected()
    {
        assertThat(gateway.isConnected()).isFalse();
        assertThat(gatewayClient.isConnected()).isFalse();

        assertThatThrownBy(() -> gatewayClient.startApplication("", __ -> TransportApplicationOnDuty.NO_OP, SerializedEventListener.NO_OP))
                .isInstanceOf(IllegalStateException.class);

        gateway.connect();
        gatewayClient.connect();
        runUntil(runner.reached(() -> gatewayClient.isConnected() && gateway.isConnected()));

        gatewayClient.startApplication("", __ -> TransportApplicationOnDuty.NO_OP, SerializedEventListener.NO_OP);
    }

    @Test
    void shouldUseAeronToInitiateTcpConnection()
    {
        gateway.connect();
        gatewayClient.connect();
        runUntil(runner.reached(() -> gatewayClient.isConnected() && gateway.isConnected()));

        final MutableBoolean hasEstablishedTcpConnection = new MutableBoolean(false);
        gatewayClient.startApplication(
                "app <-> aeron",
                transport -> new TransportApplicationOnDuty()
                {
                    @Override
                    public void onStart()
                    {
                        transport.handle(transport.command(Connect.class).set("localhost", port, 2, 5_000, rawStreaming()));
                    }

                    @Override
                    public void onEvent(final Event event)
                    {
                        if (event instanceof Connected && ((Connected)event).remotePort() == port)
                        {
                            hasEstablishedTcpConnection.set(true);
                        }
                    }
                },
                SerializedEventListener.NO_OP
        );

        assertThat(hasEstablishedTcpConnection.value).isFalse();
        runUntil(runner.reached(hasEstablishedTcpConnection::get));
        assertThat(hasEstablishedTcpConnection.value).isTrue();
    }
}