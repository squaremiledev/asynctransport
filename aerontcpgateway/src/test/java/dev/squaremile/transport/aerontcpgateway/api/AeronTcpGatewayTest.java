package dev.squaremile.transport.aerontcpgateway.api;

import org.agrona.collections.MutableBoolean;
import org.junit.jupiter.api.AfterEach;
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

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;

class AeronTcpGatewayTest
{
    private final int port = FreePort.freePort();
    private final TransportApplicationOnDuty fakeServer = FakeServer.startFakeServerListeningOn(port);

    @AfterEach
    void tearDown()
    {
        fakeServer.close();
    }

    @Test
    void shouldUseAeronToInitiateTcpConnection()
    {
        try (
                final AeronTcpGateway gateway = new AeronTcpGateway(10, 11).start();
                final AeronTcpGatewayClient client = new AeronTcpGatewayClient(gateway.aeronConnection()).start()
        )
        {
            final MutableBoolean hasEstablishedTcpConnection = new MutableBoolean(false);
            TransportApplicationOnDuty application = client.create(
                    "app <-> aeron",
                    transport -> new TransportApplicationOnDuty()
                    {
                        @Override
                        public void onStart()
                        {
                            transport.handle(transport.command(Connect.class).set(
                                    "localhost",
                                    port,
                                    2,
                                    5_000,
                                    rawStreaming()
                            ));
                        }

                        @Override
                        public void onEvent(final Event event)
                        {
                            if (event instanceof Connected &&
                                ((Connected)event).remotePort() == port)
                            {
                                hasEstablishedTcpConnection.set(true);
                            }
                        }
                    },
                    SerializedEventListener.NO_OP
            );
            application.onStart();
            assertThat(hasEstablishedTcpConnection.value).isFalse();
            runUntil(new ThingsOnDutyRunner(gateway, application).reached(hasEstablishedTcpConnection::get));
            assertThat(hasEstablishedTcpConnection.value).isTrue();
        }
    }

    @Test
    void shouldConnectToTheGateway()
    {
        try (
                final AeronTcpGateway gateway = new AeronTcpGateway(123, 45).start();
                final AeronTcpGatewayClient client = new AeronTcpGatewayClient(gateway.aeronConnection()).start()
        )
        {
            assertThat(gateway.hasClientConnected()).isFalse();
            client.create("foo", transport -> event ->
            {
            }, SerializedEventListener.NO_OP);
            runUntil(new ThingsOnDutyRunner(gateway).reached(gateway::hasClientConnected));

            assertThat(gateway.hasClientConnected()).isTrue();
        }
    }

    @Test
    void shouldProvideConnectionDetailsAfterStarted()
    {
        try (final AeronTcpGateway gateway = new AeronTcpGateway(10, 11))
        {
            assertThatThrownBy(gateway::aeronConnection).isInstanceOf(IllegalStateException.class);
            gateway.start();
            assertThat(gateway.aeronConnection().aeronContext()).isNotNull();
        }
    }

    @Test
    void shouldRequireStartedClientBeforeApplicationCanBeCreated()
    {
        try (
                final AeronTcpGateway gateway = new AeronTcpGateway(10, 11).start();
                final AeronTcpGatewayClient notStartedClient = new AeronTcpGatewayClient(gateway.aeronConnection())
        )
        {
            assertThatThrownBy(() -> notStartedClient.create("foo", transport -> event ->
            {
            }, SerializedEventListener.NO_OP)).isInstanceOf(IllegalStateException.class);

            notStartedClient.start();
            assertThat(notStartedClient.create("foo", transport -> event ->
            {
            }, SerializedEventListener.NO_OP)).isNotNull();
        }
    }
}