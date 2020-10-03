package dev.squaremile.asynctcpacceptance;

import java.io.IOException;
import java.nio.ByteBuffer;

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
import dev.squaremile.asynctcpacceptance.sampleapps.MessageEchoApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.fixtures.EventsSpy.spyAndDelegateTo;
import static dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation.SINGLE_BYTE;
import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.transport.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static java.lang.System.arraycopy;

class TcpOverRingBufferTest
{

    private final int port = freePort();
    private final SampleClient sampleClient = new SampleClient();
    private final OneToOneRingBuffer networkToUserRingBuffer = createRingBuffer();
    private final OneToOneRingBuffer userToNetworkRingBuffer = createRingBuffer();
    private final TransportFactory transportFactory = new AsyncTcp().transportFactory(NON_PROD_GRADE);

    @Test
    void shouldAcceptConnectionAndSendDataUsingTcpOverRingBuffer() throws IOException
    {
        SerializingTransport serializingTransport = new SerializingTransport(
                new ExpandableArrayBuffer(),
                32,
                new RingBufferWriter("userToNetworkRingBuffer", userToNetworkRingBuffer)
        );
        final EventsSpy userFacingAppEvents = spyAndDelegateTo(serializingTransport);
        final RingBufferApplication userFacingApp = new RingBufferApplication(
                new MessageEchoApplication(
                        serializingTransport,
                        port,
                        userFacingAppEvents,
                        SINGLE_BYTE,
                        100
                ),
                networkToUserRingBuffer
        );
        final RingBufferBackedTransport networkFacingTransport = new RingBufferBackedTransport(
                transportFactory.createMessageDrivenTransport(
                        "networkFacing", SINGLE_BYTE, new RingBufferWriter("networkToUserRingBuffer", networkToUserRingBuffer)
                ), userToNetworkRingBuffer
        );
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(networkFacingTransport, userFacingApp);

        // Given
        userFacingApp.onStart();
        runUntil(thingsOnDuty.reached(() -> !userFacingAppEvents.received().isEmpty()));
        assertEqual(userFacingAppEvents.received(), new StartedListening(port, 100));

        // When
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.connectedTo(port))));
        runUntil(thingsOnDuty.reached(() -> userFacingAppEvents.received().size() == 2));

        // Then
        assertThat(((ConnectionAccepted)userFacingAppEvents.received().get(1)).port()).isEqualTo(port);

        // DATA SENDING PART

        byte[] contentSent = byteArrayWithLong(Long.MAX_VALUE);
        byte[] contentReceived = byteArrayWithLong(0);

        // When
        sampleClient.write(contentSent);
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.read(
                contentSent.length,
                contentSent.length,
                (data, length) -> arraycopy(data, 0, contentReceived, 0, length)
        ))));

        // Then
        assertThat(contentReceived).isEqualTo(contentSent);
    }

    private byte[] byteArrayWithLong(final long value)
    {
        byte[] content = new byte[8];
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        contentBuffer.putLong(value);
        return content;
    }

    private OneToOneRingBuffer createRingBuffer()
    {
        return new OneToOneRingBuffer(new UnsafeBuffer(new byte[2048 + TRAILER_LENGTH]));
    }
}
