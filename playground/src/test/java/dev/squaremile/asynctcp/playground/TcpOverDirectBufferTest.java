package dev.squaremile.asynctcp.playground;

import java.io.IOException;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
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
    private static final int NETWORK_TO_USER_BUFFER_OFFSET = 16;
    private static final int USER_TO_NETWORK_BUFFER_OFFSET = 32;
    private static final int INITIAL_COMMAND_ID = 100;

    @Test
    void shouldAcceptConnectionUsingTcpOverDirectBuffer() throws IOException
    {
        final int port = freePort();
        final SampleClient sampleClient = new SampleClient();
        final MutableDirectBuffer userToNetworkBuffer = new ExpandableArrayBuffer();
        final MutableDirectBuffer networkToUserBuffer = new ExpandableArrayBuffer();
        final ThingsOnDutyRunner.BufferWriteSpy networkToUserWrites = new ThingsOnDutyRunner.BufferWriteSpy();
        final ThingsOnDutyRunner.BufferWriteSpy userToNetworkWrites = new ThingsOnDutyRunner.BufferWriteSpy();
        final EventsSpy userFacingAppEvents = new EventsSpy();

        final NonBLockingMessageDrivenTransport networkFacingTransport = new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        new MessageEncodingApplication(
                                new SerializingApplication(
                                        networkToUserBuffer,
                                        NETWORK_TO_USER_BUFFER_OFFSET,
                                        (buffer, offset, length) -> networkToUserWrites.add(offset, length)
                                ),
                                SINGLE_BYTE
                        ),
                        new SystemEpochClock(),
                        "networkFacing"
                ));

        final MessageOnlyDrivenApplication userFacingApp = new MessageOnlyDrivenApplication(
                new EchoApplication(
                        new SerializingTransport(
                                userToNetworkBuffer,
                                USER_TO_NETWORK_BUFFER_OFFSET,
                                (buffer, offset, length) -> userToNetworkWrites.add(offset, length)
                        ),
                        port,
                        userFacingAppEvents,
                        SINGLE_BYTE,
                        INITIAL_COMMAND_ID
                ));

        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(networkFacingTransport, userFacingApp);


        // Given the echo app started (it actually started listening on a predefined port)
        userFacingApp.onStart();
        runUntil(thingsOnDuty.reached(() -> userToNetworkWrites.count() > 0));
        // however, it does not know that it merely writes a Listen command to the buffer
        networkFacingTransport.onSerializedCommand(userToNetworkBuffer, USER_TO_NETWORK_BUFFER_OFFSET, userToNetworkWrites.entry(0).length);
        // the actual network facing transport reads the buffer and starts listening
        runUntil(thingsOnDuty.reached(() -> networkToUserWrites.count() > 0));
        // the network facing transport confirms that it started listening
        // the confirmation is written to the returning buffer
        userFacingApp.onSerializedEvent(networkToUserBuffer, NETWORK_TO_USER_BUFFER_OFFSET, networkToUserWrites.entry(0).length);
        // and the echo app receives confirmation that is started listening
        assertEqual(userFacingAppEvents.received(), new StartedListening(port, INITIAL_COMMAND_ID));


        // When
        runUntil(thingsOnDuty.reached(
                completed(() -> sampleClient.connectedTo(port)),
                () -> networkToUserWrites.count() > 1
        ));
        userFacingApp.onSerializedEvent(networkToUserBuffer, NETWORK_TO_USER_BUFFER_OFFSET, networkToUserWrites.entry(1).length);

        // Then
        assertThat(userFacingAppEvents.received()).hasSize(2);
        ConnectionAccepted connectionAccepted = (ConnectionAccepted)userFacingAppEvents.received().get(1);
        assertThat(connectionAccepted.port()).isEqualTo(port);
    }
}
