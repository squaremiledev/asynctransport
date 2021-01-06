package dev.squaremile.asynctcp.internal.serialization.delineation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.CloseConnection;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.transport.events.ConnectionClosed;
import dev.squaremile.asynctcp.api.transport.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.api.transport.events.DataReceived;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.api.transport.events.StoppedListening;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static java.nio.ByteBuffer.wrap;
import static java.util.Arrays.asList;

class DelineationApplicationTest
{

    private final ApplicationSpy spy = new ApplicationSpy();

    static DirectBuffer wrapDirect(byte[] array)
    {
        return new UnsafeBuffer(array);
    }

    private static ConnectionResetByPeer connectionResetByPeer(final ConnectionId connectionId)
    {
        return new ConnectionResetByPeer(connectionId.port(), connectionId.connectionId(), -1);
    }

    private static ConnectionClosed connectionClosed(final ConnectionId connectionId)
    {
        return new ConnectionClosed(connectionId.port(), connectionId.connectionId(), -1);
    }

    @Test
    void shouldDelegateEventsToUnderlyingApplication()
    {
        // Given
        final DelineationApplication app = new DelineationApplication(spy);
        assertThat(spy.all()).isEmpty();

        // When
        app.onStart();
        app.onStop();
        app.work();
        app.handle(new Listen().set(99, connectionId().port(), fixedLengthDelineation(1)));
        app.onEvent(new StartedListening(8888, 5, fixedLengthDelineation(1)));

        // Then
        assertEquals(
                spy.all(),
                "onStart",
                "onStop",
                "work",
                new StartedListening(8888, 5, fixedLengthDelineation(1))
        );
    }

    @Test
    void shouldPassMessagesThrough()
    {
        final DelineationApplication app = new DelineationApplication(spy);

        // When
        MessageReceived messageReceived1 = new MessageReceived(new ConnectionIdValue(8899, 4)).set(wrapDirect(new byte[]{1, 2, 3, 4, 5, 6, 7}), 5);
        MessageReceived messageReceived2 = new MessageReceived(new ConnectionIdValue(6234, 5)).set(wrapDirect(new byte[]{0}), 1);
        app.onEvent(messageReceived1);
        app.onEvent(messageReceived2);

        // Then
        assertEquals(spy.all(), messageReceived1.copy(), messageReceived2.copy());
    }

    @Test
    void shouldNotDelineateRawStreaming()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(98, anotherConnectionId().port(), rawStreaming()));
        app.onEvent(new StartedListening(anotherConnectionId().port(), 98, rawStreaming()));
        app.onEvent(connectionAccepted(anotherConnectionId(), rawStreaming()));
        app.handle(new Listen().set(99, connectionId().port(), rawStreaming()));
        app.onEvent(new StartedListening(connectionId().port(), 99, rawStreaming()));
        app.onEvent(connectionAccepted(connectionId(), rawStreaming()));

        // When
        app.onEvent(new DataReceived(connectionId(), 2, 2, 30, wrap(new byte[]{5, 7})));
        app.onEvent(new DataReceived(anotherConnectionId(), 1, 1, 30, wrap(new byte[]{6})));

        // Then
        assertEquals(
                spy.all(),
                new StartedListening(anotherConnectionId().port(), 98, rawStreaming()),
                connectionAccepted(anotherConnectionId(), rawStreaming()),
                new StartedListening(connectionId().port(), 99, rawStreaming()),
                connectionAccepted(connectionId(), rawStreaming()),
                new MessageReceived(connectionId()).set(wrapDirect(new byte[]{5, 7}), 2).copy(),
                new MessageReceived(anotherConnectionId()).set(wrapDirect(new byte[]{6}), 1).copy()
        );
    }

    @Test
    void shouldEncodeSingleByteDataAsMessage()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(98, anotherConnectionId().port(), fixedLengthDelineation(1)));
        app.onEvent(new StartedListening(anotherConnectionId().port(), 98, fixedLengthDelineation(1)));
        app.onEvent(connectionAccepted(anotherConnectionId(), fixedLengthDelineation(1)));
        app.handle(new Listen().set(99, connectionId().port(), fixedLengthDelineation(1)));
        app.onEvent(new StartedListening(connectionId().port(), 99, fixedLengthDelineation(1)));
        app.onEvent(connectionAccepted(connectionId(), fixedLengthDelineation(1)));

        // When
        app.onEvent(new DataReceived(connectionId(), 1, 1, 30, wrap(new byte[]{5})));
        app.onEvent(new DataReceived(anotherConnectionId(), 1, 1, 30, wrap(new byte[]{6})));

        // Then
        assertEquals(
                spy.all(),
                new StartedListening(anotherConnectionId().port(), 98, fixedLengthDelineation(1)),
                connectionAccepted(anotherConnectionId(), fixedLengthDelineation(1)),
                new StartedListening(connectionId().port(), 99, fixedLengthDelineation(1)),
                connectionAccepted(connectionId(), fixedLengthDelineation(1)),
                new MessageReceived(connectionId()).set(wrapDirect(new byte[]{5}), 1).copy(),
                new MessageReceived(anotherConnectionId()).set(wrapDirect(new byte[]{6}), 1).copy()
        );
    }

    @Test
    void shouldEncodeMultipleBytesAsMultipleIndividualMessages()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(99, connectionId().port(), fixedLengthDelineation(1)));
        app.onEvent(new StartedListening(connectionId().port(), 99, fixedLengthDelineation(1)));
        app.onEvent(new ConnectionAccepted(connectionId().port(), 100, "localhost", 33160, 6, 65536, 1313280, fixedLengthDelineation(1)));

        // When
        DataReceived event = new DataReceived(connectionId().port(), 6, 1, 3, 30, wrap(new byte[]{1, 2, 3}));
        app.onEvent(event);

        // Then
        assertEquals(
                spy.all(),
                new StartedListening(connectionId().port(), 99, fixedLengthDelineation(1)),
                new ConnectionAccepted(connectionId().port(), 100, "localhost", 33160, 6, 65536, 1313280, fixedLengthDelineation(1)),
                new MessageReceived(event).set(wrapDirect(new byte[]{1}), 1).copy(),
                new MessageReceived(event).set(wrapDirect(new byte[]{2}), 1).copy(),
                new MessageReceived(event).set(wrapDirect(new byte[]{3}), 1).copy()
        );
    }

    @Test
    void shouldSupportMultipleConnections()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(100, connectionId().port(), fixedLengthDelineation(1)));
        app.onEvent(new StartedListening(connectionId().port(), 100, fixedLengthDelineation(1)));
        app.onEvent(new ConnectionAccepted(connectionId().port(), 100, "localhost", 33160, 8, 65536, 1313280, fixedLengthDelineation(1)));
        app.handle(new Listen().set(200, 8809, fixedLengthDelineation(1)));
        app.onEvent(new StartedListening(8809, 200, fixedLengthDelineation(1)));
        app.onEvent(new ConnectionAccepted(8809, 200, "127.0.0.1", 33170, 9, 1234, 5678, fixedLengthDelineation(1)));

        // When
        app.onEvent(new DataReceived(8809, 9, 1, 1, 30, wrap(new byte[]{90})));
        app.onEvent(new DataReceived(connectionId().port(), 8, 1, 1, 40, wrap(new byte[]{80})));

        // Then
        assertEquals(
                spy.messagesReceived(),
                new MessageReceived(new ConnectionIdValue(8809, 9)).set(wrapDirect(new byte[]{90}), 1).copy(),
                new MessageReceived(new ConnectionIdValue(connectionId().port(), 8)).set(wrapDirect(new byte[]{80}), 1).copy()

        );
    }

    @Test
    void shouldSupportMultipleInitiatedConnections()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Connect().set("localhost", 8888, 100, 500, fixedLengthDelineation(1)));
        app.onEvent(new Connected(8888, 100, "remoteHost1", 5555, 1, 56000, 80000, fixedLengthDelineation(1)));
        app.handle(new Connect().set("127.0.0.1", 8889, 101, 500, fixedLengthDelineation(1)));
        app.onEvent(new Connected(8889, 101, "remoteHost2", 5556, 2, 46000, 90000, fixedLengthDelineation(1)));

        // When
        app.onEvent(new DataReceived(8888, 1, 1, 1, 30, wrap(new byte[]{88})));
        app.onEvent(new DataReceived(8889, 2, 1, 1, 40, wrap(new byte[]{89})));

        // Then
        assertEquals(
                spy.messagesReceived(),
                new MessageReceived(new ConnectionIdValue(8888, 1)).set(wrapDirect(new byte[]{88}), 1).copy(),
                new MessageReceived(new ConnectionIdValue(8889, 2)).set(wrapDirect(new byte[]{89}), 1).copy()
        );
    }

    @Test
    void shouldRememberDelineationBetweenAfterReconnectedToTheListeningSocket()
    {
        ConnectionId resetConnectionId = connectionId();
        ConnectionIdValue reconnectedId = new ConnectionIdValue(resetConnectionId.port(), resetConnectionId.connectionId() + 1);
        ConnectionIdValue anotherConnectionId = new ConnectionIdValue(resetConnectionId.port(), resetConnectionId.connectionId() + 2);
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(99, resetConnectionId.port(), fixedLengthDelineation(8)));
        app.onEvent(new StartedListening(resetConnectionId.port(), 99, fixedLengthDelineation(8)));
        app.onEvent(connectionAccepted(resetConnectionId, fixedLengthDelineation(8)));
        app.onEvent(connectionResetByPeer(resetConnectionId));
        app.onEvent(connectionAccepted(reconnectedId, fixedLengthDelineation(8)));
        app.onEvent(connectionAccepted(anotherConnectionId, fixedLengthDelineation(8)));

        // When
        app.onEvent(new DataReceived(reconnectedId, 8, 8, 30, wrap(byteArrayWithLong())));
        app.onEvent(new DataReceived(anotherConnectionId, 8, 8, 30, wrap(byteArrayWithLong())));

        // Then
        assertEquals(
                spy.messagesReceived(),
                new MessageReceived(reconnectedId).set(wrapDirect(byteArrayWithLong()), 8).copy(),
                new MessageReceived(anotherConnectionId).set(wrapDirect(byteArrayWithLong()), 8).copy()
        );
    }

    @Test
    void shouldNotHandleDroppedConnections()
    {
        ConnectionId resetConnectionId = connectionId();
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(99, resetConnectionId.port(), fixedLengthDelineation(8)));
        app.onEvent(new StartedListening(resetConnectionId.port(), 99, fixedLengthDelineation(8)));
        app.onEvent(connectionAccepted(resetConnectionId, fixedLengthDelineation(8)));
        app.onEvent(connectionResetByPeer(resetConnectionId));

        // When
        app.onEvent(new DataReceived(resetConnectionId, 8, 8, 30, wrap(byteArrayWithLong())));

        // Then
        assertThat(spy.messagesReceived()).isEmpty();
    }

    @Test
    void shouldSupportOtherIntsAsDelineationMechanisms()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(99, connectionId().port(), fixedLengthDelineation(4)));
        app.onEvent(new StartedListening(connectionId().port(), 99, fixedLengthDelineation(4)));
        app.onEvent(connectionAccepted(connectionId(), fixedLengthDelineation(4)));

        // When
        app.onEvent(new DataReceived(connectionId(), 4, 4, 30, wrap(byteArrayWithInt())));

        // Then
        assertEquals(
                spy.messagesReceived(),
                new MessageReceived(connectionId()).set(wrapDirect(byteArrayWithInt()), 4).copy()
        );
    }

    @Test
    void shouldNotSupportReconnectingToTheNoLongerListeningPort()
    {
        ConnectionIdValue noLongerListeningPortConnectionId = new ConnectionIdValue(connectionId().port(), connectionId().connectionId() + 1);
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(99, connectionId().port(), fixedLengthDelineation(8)));
        app.onEvent(new StartedListening(connectionId().port(), 99, fixedLengthDelineation(8)));
        app.onEvent(connectionAccepted(connectionId(), fixedLengthDelineation(8)));
        app.onEvent(new StoppedListening(connectionId().port(), 100));
        app.onEvent(connectionAccepted(noLongerListeningPortConnectionId, fixedLengthDelineation(8)));

        // When
        app.onEvent(new DataReceived(connectionId(), 8, 8, 30, wrap(byteArrayWithLong())));
        app.onEvent(new DataReceived(noLongerListeningPortConnectionId, 8, 8, 30, wrap(byteArrayWithLong())));

        // Then
        assertEquals(
                spy.messagesReceived(),
                new MessageReceived(connectionId()).set(wrapDirect(byteArrayWithLong()), 8).copy()
        );
    }

    @Test
    void shouldNotSupportDisconnectedConnections()
    {
        // Given
        ConnectionId connectionId1 = new ConnectionIdValue(8801, 1);
        ConnectionId connectionId2 = new ConnectionIdValue(8802, 2);
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Connect().set("localhost1", 5551, 201, 500, fixedLengthDelineation(8)));
        app.onEvent(new Connected(connectionId1.port(), 201, "remoteHost1", 5551, connectionId1.connectionId(), 56000, 80000, fixedLengthDelineation(8)));
        app.handle(new Connect().set("localhost2", 5552, 202, 500, fixedLengthDelineation(8)));
        app.onEvent(new Connected(connectionId2.port(), 202, "remoteHost2", 5552, connectionId2.connectionId(), 56000, 80000, fixedLengthDelineation(8)));
        app.handle(new CloseConnection(connectionId2));
        app.onEvent(connectionResetByPeer(connectionId2));
        app.onEvent(connectionClosed(connectionId1));

        // When
        app.onEvent(new DataReceived(connectionId1, 8, 8, 30, wrap(byteArrayWithLong())));
        app.onEvent(new DataReceived(connectionId2, 8, 8, 30, wrap(byteArrayWithLong())));

        // Then
        assertThat(spy.messagesReceived()).isEmpty();
    }

    @Test
    void shouldDelineatePerAcceptedConnection()
    {
        // Given
        final DelineationApplication app = new DelineationApplication(spy);
        ConnectionIdValue connectedSingleByteConnectionId = new ConnectionIdValue(9801, 11);
        ConnectionIdValue connectedLongsConnectionId = new ConnectionIdValue(9802, 12);
        ConnectionIdValue connectedIntegersConnectionId = new ConnectionIdValue(9803, 13);
        ConnectionIdValue acceptedSingleByteConnectionId = new ConnectionIdValue(8801, 1);
        ConnectionIdValue acceptedLongsConnectionId = new ConnectionIdValue(8802, 2);
        ConnectionIdValue acceptedIntegersConnectionId = new ConnectionIdValue(8803, 3);
        app.handle(new Connect().set("localhost1", 5551, 201, 500, fixedLengthDelineation(1)));
        app.handle(new Listen().set(101, acceptedSingleByteConnectionId.port(), fixedLengthDelineation(1)));
        app.onEvent(new StartedListening(acceptedSingleByteConnectionId.port(), 101, fixedLengthDelineation(1)));
        app.handle(new Connect().set("localhost2", 5552, 202, 500, fixedLengthDelineation(8)));
        app.onEvent(new Connected(connectedSingleByteConnectionId.port(), 201, "remoteHost1", 5551, connectedSingleByteConnectionId.connectionId(), 56000, 80000, fixedLengthDelineation(1)));
        app.onEvent(connectionAccepted(acceptedSingleByteConnectionId, fixedLengthDelineation(1)));
        app.handle(new Listen().set(102, acceptedLongsConnectionId.port(), fixedLengthDelineation(8)));
        app.handle(new Listen().set(103, acceptedIntegersConnectionId.port(), fixedLengthDelineation(4)));
        app.handle(new Connect().set("localhost3", 5553, 203, 500, fixedLengthDelineation(4)));
        app.onEvent(new StartedListening(acceptedLongsConnectionId.port(), 102, fixedLengthDelineation(8)));
        app.onEvent(new StartedListening(acceptedIntegersConnectionId.port(), 103, fixedLengthDelineation(4)));
        app.onEvent(connectionAccepted(acceptedLongsConnectionId, fixedLengthDelineation(8)));
        app.onEvent(new Connected(connectedIntegersConnectionId.port(), 203, "remoteHost3", 5553, connectedIntegersConnectionId.connectionId(), 56000, 80000, fixedLengthDelineation(4)));
        app.onEvent(new Connected(connectedLongsConnectionId.port(), 202, "remoteHost2", 5552, connectedLongsConnectionId.connectionId(), 56000, 80000, fixedLengthDelineation(8)));
        app.onEvent(connectionAccepted(acceptedIntegersConnectionId, fixedLengthDelineation(4)));

        // When
        app.onEvent(new DataReceived(connectedLongsConnectionId, 8, 8, 30, wrap(byteArrayWithLong())));
        app.onEvent(new DataReceived(acceptedSingleByteConnectionId, 4, 4, 30, wrap(byteArrayWithInt())));
        app.onEvent(new DataReceived(acceptedLongsConnectionId, 8, 8, 30, wrap(byteArrayWithLong())));
        app.onEvent(new DataReceived(connectedSingleByteConnectionId, 4, 4, 30, wrap(byteArrayWithInt())));
        app.onEvent(new DataReceived(acceptedIntegersConnectionId, 4, 4, 30, wrap(byteArrayWithInt())));
        app.onEvent(new DataReceived(connectedIntegersConnectionId, 4, 4, 30, wrap(byteArrayWithInt())));


        // Then
        assertEquals(
                spy.messagesReceived(),
                new MessageReceived(connectedLongsConnectionId).set(wrapDirect(byteArrayWithLong()), 8).copy(),

                new MessageReceived(acceptedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[0]}), 1).copy(),
                new MessageReceived(acceptedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[1]}), 1).copy(),
                new MessageReceived(acceptedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[2]}), 1).copy(),
                new MessageReceived(acceptedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[3]}), 1).copy(),

                new MessageReceived(acceptedLongsConnectionId).set(wrapDirect(byteArrayWithLong()), 8).copy(),

                new MessageReceived(connectedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[0]}), 1).copy(),
                new MessageReceived(connectedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[1]}), 1).copy(),
                new MessageReceived(connectedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[2]}), 1).copy(),
                new MessageReceived(connectedSingleByteConnectionId).set(wrapDirect(new byte[]{byteArrayWithInt()[3]}), 1).copy(),

                new MessageReceived(acceptedIntegersConnectionId).set(wrapDirect(byteArrayWithInt()), 4).copy(),

                new MessageReceived(connectedIntegersConnectionId).set(wrapDirect(byteArrayWithInt()), 4).copy()
        );
    }

    private void assertEquals(final List<Object> actual, final Object... expected)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(asList(expected));
    }

    private byte[] byteArrayWithLong()
    {
        return new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
    }

    private byte[] byteArrayWithInt()
    {
        return new byte[]{11, 12, 13, 14};
    }

    ConnectionId connectionId()
    {
        return new ConnectionIdValue(8808, 5);
    }

    ConnectionId anotherConnectionId()
    {
        return new ConnectionIdValue(8809, 6);
    }

    private ConnectionAccepted connectionAccepted(final ConnectionId connectionId, final Delineation delineation)
    {
        return new ConnectionAccepted(connectionId, 51, "localhost", 33160, 65536, 1313280, delineation);
    }

    private static class ApplicationSpy implements TransportApplicationOnDuty
    {

        private final List<Object> invoked = new ArrayList<>();

        List<Object> all()
        {
            return invoked;
        }

        List<Object> messagesReceived()
        {
            return invoked.stream().filter(o -> o instanceof MessageReceived).collect(Collectors.toList());
        }

        @Override
        public void onStart()
        {
            invoked.add("onStart");
        }

        @Override
        public void onStop()
        {
            invoked.add("onStop");
        }

        @Override
        public void work()
        {
            invoked.add("work");
        }

        @Override
        public void onEvent(final Event event)
        {
            invoked.add(event.copy());
        }
    }
}