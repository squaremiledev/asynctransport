package dev.squaremile.asynctcp.playground;

import java.io.IOException;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.SystemEpochClock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.events.StartedListening;
import dev.squaremile.asynctcp.internal.nonblockingimpl.NonBlockingTransport;
import dev.squaremile.asynctcp.serialization.SerializingApplication;
import dev.squaremile.asynctcp.serialization.SerializingTransport;
import dev.squaremile.asynctcp.testfixtures.network.SampleClient;
import dev.squaremile.asynctcpacceptance.sampleapps.EchoApplication;
import dev.squaremile.asynctcpacceptance.sampleapps.MessageEncodingApplication;

import static dev.squaremile.asynctcp.api.values.PredefinedTransportEncoding.SINGLE_BYTE;
import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.testfixtures.Worker.runUntil;

class TcpOverDirectBufferTest
{
    @Test
    void shouldAcceptConnectionUsingTcpOverDirectBuffer() throws IOException
    {
        final int port = freePort();
        final SampleClient sampleClient = new SampleClient();
        final SerializedMessagesSpy networkToUserWrites = new SerializedMessagesSpy();
        final SerializedMessagesSpy userToNetworkWrites = new SerializedMessagesSpy();
        final EventsSpy userFacingAppEvents = new EventsSpy();

        final NonBLockingMessageDrivenTransport networkFacingTransport = new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        new MessageEncodingApplication(
                                new SerializingApplication(
                                        new ExpandableArrayBuffer(),
                                        16,
                                        networkToUserWrites
                                ),
                                SINGLE_BYTE
                        ),
                        new SystemEpochClock(),
                        "networkFacing"
                ));

        final MessageOnlyDrivenApplication userFacingApp = new MessageOnlyDrivenApplication(
                new EchoApplication(
                        new SerializingTransport(
                                new ExpandableArrayBuffer(),
                                32,
                                userToNetworkWrites
                        ),
                        port,
                        userFacingAppEvents,
                        SINGLE_BYTE,
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
        assertEqual(userFacingAppEvents.received(), new StartedListening(port, 100));


        // When
        runUntil(thingsOnDuty.reached(
                completed(() -> sampleClient.connectedTo(port)),
                () -> networkToUserWrites.count() > 1
        ));
        userFacingApp.onSerialized(networkToUserWrites.buffer(), networkToUserWrites.entry(1).offset, networkToUserWrites.entry(1).length);

        // Then
        assertThat(userFacingAppEvents.received()).hasSize(2);
        ConnectionAccepted connectionAccepted = (ConnectionAccepted)userFacingAppEvents.received().get(1);
        assertThat(connectionAccepted.port()).isEqualTo(port);
    }
}
