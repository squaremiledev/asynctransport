package dev.squaremile.asynctcpacceptance;

import java.io.IOException;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.junit.jupiter.api.Test;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportFactory;
import dev.squaremile.asynctcp.fixtures.EventsSpy;
import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.testfixtures.network.SampleClient;
import dev.squaremile.asynctcpacceptance.sampleapps.EchoApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.PredefinedTransportEncoding.SINGLE_BYTE;
import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.transport.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;

class TcpOverRingBufferTest
{
    private final TransportFactory asyncTcpTransportFactory = new AsyncTcp().transportFactory(NON_PROD_GRADE);

    @Test
    void shouldAcceptConnectionUsingTcpOverDirectBuffer() throws IOException
    {
        final int port = freePort();
        final SampleClient sampleClient = new SampleClient();
        final EventsSpy userFacingAppEvents = new EventsSpy();
        final OneToOneRingBuffer networkToUserRingBuffer = createRingBuffer();
        final OneToOneRingBuffer userToNetworkRingBuffer = createRingBuffer();

        final MessageDrivenTransport networkFacingTransport = asyncTcpTransportFactory.createMessageDrivenTransport(
                "networkFacing",
                SINGLE_BYTE,
                (sourceBuffer, sourceOffset, length) -> networkToUserRingBuffer.write(1, sourceBuffer, sourceOffset, length)
        );
        final MessageOnlyDrivenApplication userFacingApp = new MessageOnlyDrivenApplication(
                new EchoApplication(
                        new SerializingTransport(
                                new ExpandableArrayBuffer(),
                                32,
                                (sourceBuffer, sourceOffset, length) -> userToNetworkRingBuffer.write(1, sourceBuffer, sourceOffset, length)
                        ),
                        port,
                        userFacingAppEvents,
                        SINGLE_BYTE,
                        100
                ));
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(networkFacingTransport, userFacingApp);


        // Given
        userFacingApp.onStart();
        runUntil(thingsOnDuty.reached(() -> userToNetworkRingBuffer.read((msgTypeId, buffer, index, length) -> networkFacingTransport.onSerialized(buffer, index, length)) > 0));
        runUntil(thingsOnDuty.reached(() -> networkToUserRingBuffer.read((msgTypeId, buffer, index, length) -> userFacingApp.onSerialized(buffer, index, length)) > 0));
        assertEqual(userFacingAppEvents.received(), new StartedListening(port, 100));

        // When
        runUntil(thingsOnDuty.reached(
                completed(() -> sampleClient.connectedTo(port)),
                () -> networkToUserRingBuffer.read((msgTypeId, buffer, index, length) -> userFacingApp.onSerialized(buffer, index, length)) > 0
        ));

        // Then
        assertThat(userFacingAppEvents.received()).hasSize(2);
        ConnectionAccepted connectionAccepted = (ConnectionAccepted)userFacingAppEvents.received().get(1);
        assertThat(connectionAccepted.port()).isEqualTo(port);
    }

    private OneToOneRingBuffer createRingBuffer()
    {
        return new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 + TRAILER_LENGTH]));
    }
}
