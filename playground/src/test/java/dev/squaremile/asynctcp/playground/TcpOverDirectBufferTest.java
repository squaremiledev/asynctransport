package dev.squaremile.asynctcp.playground;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.SystemEpochClock;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.api.events.StartedListening;
import dev.squaremile.asynctcp.internal.nonblockingimpl.NonBlockingTransport;
import dev.squaremile.asynctcp.serialization.SerializingApplication;
import dev.squaremile.asynctcp.serialization.SerializingTransport;
import dev.squaremile.asynctcpacceptance.sampleapps.EchoApplication;
import dev.squaremile.asynctcpacceptance.sampleapps.MessageEncodingApplication;

import static dev.squaremile.asynctcp.api.values.PredefinedTransportEncoding.SINGLE_BYTE;
import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.testfixtures.Worker.runUntil;

class TcpOverDirectBufferTest
{

    private static final MutableDirectBuffer USER_TO_NETWORK_BUFFER = new UnsafeBuffer(new byte[128]);
    private static final MutableDirectBuffer NETWORK_TO_USER_BUFFER = new UnsafeBuffer(new byte[256]);
    private static final int NETWORK_TO_USER_BUFFER_OFFSET = 16;
    private static final int USER_TO_NETWORK_BUFFER_OFFSET = 32;
    private static final int INITIAL_COMMAND_ID = 100;

    private final int port = freePort();

    @Test
    void shouldUseTcpOverDirectBuffer() throws IOException
    {
        // TODO #6: Two instances, one for each direction, of org.agrona.concurrent.ringbuffer.OneToOneRingBuffer
        // seems to be a good fit here

        // Given
        final BufferWriteSpy networkToUserWrites = new BufferWriteSpy();
        final NonBLockingMessageDrivenTransport networkFacingTransport = new NonBLockingMessageDrivenTransport(
                new NonBlockingTransport(
                        new MessageEncodingApplication(
                                new SerializingApplication(
                                        NETWORK_TO_USER_BUFFER,
                                        NETWORK_TO_USER_BUFFER_OFFSET,
                                        (buffer, offset, length) -> networkToUserWrites.add(offset, length)
                                ),
                                SINGLE_BYTE
                        ),
                        new SystemEpochClock(),
                        "networkFacing"
                ));


        final BufferWriteSpy userToNetworkWrites = new BufferWriteSpy();
        final EventsSpy userFacingAppEvents = new EventsSpy();
        final MessageOnlyDrivenApplication userFacingApp = new MessageOnlyDrivenApplication(
                new EchoApplication(
                        new SerializingTransport(
                                USER_TO_NETWORK_BUFFER,
                                USER_TO_NETWORK_BUFFER_OFFSET,
                                (buffer, offset, length) -> userToNetworkWrites.add(offset, length)
                        ),
                        port,
                        userFacingAppEvents,
                        SINGLE_BYTE,
                        INITIAL_COMMAND_ID
                ));


        // When the echo app starts, it starts listening on a predefined port
        userFacingApp.onStart();
        // however, it does not know that it merely writes a Listen command to the buffer
        networkFacingTransport.onSerializedCommand(USER_TO_NETWORK_BUFFER, USER_TO_NETWORK_BUFFER_OFFSET, userToNetworkWrites.entry(0).length);
        // the actual network facing transport reads the buffer and starts listening
        runUntil(() ->
                 {
                     networkFacingTransport.work();
                     userFacingApp.work();
                     return !networkToUserWrites.entries().isEmpty();
                 });
        // the network facing transport confirms that it started listening
        // the confirmation is written to the returning buffer
        userFacingApp.onSerializedEvent(NETWORK_TO_USER_BUFFER, NETWORK_TO_USER_BUFFER_OFFSET, networkToUserWrites.entry(0).length);

        // Then the echo app receives confirmation that is started listening
        assertEqual(userFacingAppEvents.received(), new StartedListening(port, INITIAL_COMMAND_ID));
    }

    private static class BufferWriteSpy
    {
        private final List<WrittenEntries> entries = new ArrayList<>();

        void add(final int offset, final int length)
        {
            entries.add(new WrittenEntries(offset, length));
        }

        public List<WrittenEntries> entries()
        {
            return entries;
        }

        public WrittenEntries entry(int index)
        {
            return entries.get(index);
        }

        static class WrittenEntries
        {
            final int offset;
            final int length;

            WrittenEntries(final int offset, final int length)
            {
                this.offset = offset;
                this.length = length;
            }
        }
    }

}
