package dev.squaremile.transport.aerontcpgateway;

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
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;

class AeronConnectionTest
{

    private final int port = FreePort.freePort();
    private MediaDriver mediaDriver;
    private TransportApplicationOnDuty fakeServer;
    private AeronTcpGateway aeronTcpGateway;
    private AeronGatewayClient aeronGatewayClient;
    private ThingsOnDutyRunner runner;

    @BeforeEach
    void setUp()
    {
        mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED).dirDeleteOnShutdown(true));
        fakeServer = FakeServer.startFakeServerListeningOn(port);
        aeronTcpGateway = new AeronTcpGateway(new AeronConnection(10, 11, mediaDriver.aeronDirectoryName()));
        aeronGatewayClient = new AeronGatewayClient(new AeronConnection(10, 11, mediaDriver.aeronDirectoryName()));
        runner = new ThingsOnDutyRunner(fakeServer, aeronGatewayClient, aeronTcpGateway);
    }

    @AfterEach
    void tearDown()
    {
        aeronGatewayClient.close();
        aeronTcpGateway.close();
        fakeServer.close();
        mediaDriver.close();
    }

    @Test
    void shouldConnectToTheGateway()
    {
        assertThat(aeronTcpGateway.isConnected()).isFalse();
        assertThat(aeronGatewayClient.isConnected()).isFalse();

        aeronTcpGateway.connect();
        aeronGatewayClient.connect();
        runUntil(runner.reached(() -> aeronGatewayClient.isConnected() && aeronTcpGateway.isConnected()));

        assertThat(aeronTcpGateway.isConnected()).isTrue();
        assertThat(aeronGatewayClient.isConnected()).isTrue();
    }

    @Test
    void shouldNotAllowToStartApplicationUntilGatewayIsConnected()
    {
        assertThat(aeronTcpGateway.isConnected()).isFalse();
        assertThat(aeronGatewayClient.isConnected()).isFalse();

        assertThatThrownBy(() -> aeronGatewayClient.startApplication("", __ -> TransportApplicationOnDuty.NO_OP, SerializedEventListener.NO_OP))
                .isInstanceOf(IllegalStateException.class);

        aeronTcpGateway.connect();
        aeronGatewayClient.connect();
        runUntil(runner.reached(() -> aeronGatewayClient.isConnected() && aeronTcpGateway.isConnected()));

        aeronGatewayClient.startApplication("", __ -> TransportApplicationOnDuty.NO_OP, SerializedEventListener.NO_OP);
    }

    @Test
    void tcpSandbox()
    {
        aeronTcpGateway.connect();
        aeronGatewayClient.connect();
        runUntil(runner.reached(() -> aeronGatewayClient.isConnected() && aeronTcpGateway.isConnected()));

        final MutableBoolean hasEstablishedTcpConnection = new MutableBoolean(false);
        aeronGatewayClient.startApplication(
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