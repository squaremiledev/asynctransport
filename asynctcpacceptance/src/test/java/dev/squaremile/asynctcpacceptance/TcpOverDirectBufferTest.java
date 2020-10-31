package dev.squaremile.asynctcpacceptance;

import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.fixtures.SerializedMessagesSpy;
import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.internal.NonProdGradeTransportFactory;
import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcp.transport.testfixtures.EventsSpy;
import dev.squaremile.asynctcp.transport.testfixtures.network.SampleClient;
import dev.squaremile.asynctcpacceptance.sampleapps.MessageEchoApplication;

import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.transport.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.transport.testfixtures.EventsSpy.spy;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;

class TcpOverDirectBufferTest
{

    private final NonProdGradeTransportFactory asyncTcpTransportFactory = new NonProdGradeTransportFactory();

    @Test
    void shouldAcceptConnectionUsingTcpOverDirectBuffer()
    {
        final int port = freePort();
        final SampleClient sampleClient = new SampleClient();
        final SerializedMessagesSpy networkToUserWrites = new SerializedMessagesSpy();
        final SerializedMessagesSpy userToNetworkWrites = new SerializedMessagesSpy();
        final EventsSpy userFacingAppEvents = spy();
        final MessageDrivenTransport networkFacingTransport = asyncTcpTransportFactory.create(
                "networkFacing", networkToUserWrites);
        final MessageOnlyDrivenApplication userFacingApp = new MessageOnlyDrivenApplication(
                new MessageEchoApplication(
                        new SerializingTransport(
                                new ExpandableArrayBuffer(),
                                32,
                                userToNetworkWrites
                        ),
                        port,
                        userFacingAppEvents,
                        new Delineation(Delineation.Type.FIXED_LENGTH, 0, 1, ""),
                        100
                ));
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(networkFacingTransport, userFacingApp);


        // Given the echo app started (it actually started listening on a predefined port)
        userFacingApp.onStart();
        runUntil(thingsOnDuty.reached(() -> userToNetworkWrites.count() > 0));
        // however, it does not know that it merely writes a Listen command to the buffer
        networkFacingTransport.onSerialized(userToNetworkWrites.buffer(), userToNetworkWrites.entry(0).offset, userToNetworkWrites.entry(0).length);
        // the actual network facing transport reads the buffer and starts listening
        runUntil(thingsOnDuty.reached(() -> networkToUserWrites.count() > 0));
        // the network facing transport confirms that it started listening
        // the confirmation is written to the returning buffer
        userFacingApp.onSerialized(networkToUserWrites.buffer(), networkToUserWrites.entry(0).offset, networkToUserWrites.entry(0).length);
        // and the echo app receives confirmation that is started listening
        assertEqual(userFacingAppEvents.all(), new StartedListening(port, 100, new Delineation(Delineation.Type.FIXED_LENGTH, 0, 1, "")));


        // When
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.connectedTo(port))));
        runUntil(thingsOnDuty.reached(() -> networkToUserWrites.count() > 1));
        userFacingApp.onSerialized(networkToUserWrites.buffer(), networkToUserWrites.entry(1).offset, networkToUserWrites.entry(1).length);

        // Then
        assertThat(userFacingAppEvents.all()).hasSize(2);
        ConnectionAccepted connectionAccepted = (ConnectionAccepted)userFacingAppEvents.all().get(1);
        assertThat(connectionAccepted.port()).isEqualTo(port);
    }
}
