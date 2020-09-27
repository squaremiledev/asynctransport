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
import dev.squaremile.asynctcp.serialization.internal.SerializingTransport;
import dev.squaremile.asynctcp.serialization.internal.messaging.RingBufferApplication;
import dev.squaremile.asynctcp.serialization.internal.messaging.RingBufferBackedTransport;
import dev.squaremile.asynctcp.serialization.internal.messaging.RingBufferWriter;
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

    private final int port = freePort();
    private final SampleClient sampleClient = new SampleClient();
    private final EventsSpy userFacingAppEvents = new EventsSpy();
    private final OneToOneRingBuffer networkToUserRingBuffer = createRingBuffer();
    private final OneToOneRingBuffer userToNetworkRingBuffer = createRingBuffer();
    private final TransportFactory transportFactory = new AsyncTcp().transportFactory(NON_PROD_GRADE);

    @Test
    void shouldAcceptConnectionUsingTcpOverRingBuffer() throws IOException
    {
        final RingBufferApplication userFacingApp = new RingBufferApplication(
                new EchoApplication(
                        new SerializingTransport(
                                new ExpandableArrayBuffer(),
                                32,
                                new RingBufferWriter(userToNetworkRingBuffer)
                        ),
                        port,
                        userFacingAppEvents,
                        SINGLE_BYTE,
                        100
                ),
                networkToUserRingBuffer
        );
        final RingBufferBackedTransport transport = new RingBufferBackedTransport(
                transportFactory.createMessageDrivenTransport(
                        "networkFacing", SINGLE_BYTE, new RingBufferWriter(networkToUserRingBuffer)
                ), userToNetworkRingBuffer
        );
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(transport, userFacingApp);

        // Given
        userFacingApp.onStart();
        runUntil(thingsOnDuty.reached(() -> !userFacingAppEvents.received().isEmpty()));
        assertEqual(userFacingAppEvents.received(), new StartedListening(port, 100));

        // When
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.connectedTo(port))));
        runUntil(thingsOnDuty.reached(() -> userFacingAppEvents.received().size() == 2));

        // Then
        assertThat(((ConnectionAccepted)userFacingAppEvents.received().get(1)).port()).isEqualTo(port);
    }

    private OneToOneRingBuffer createRingBuffer()
    {
        return new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 + TRAILER_LENGTH]));
    }
}
