package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

import static dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation.SINGLE_BYTE;
import static java.nio.ByteBuffer.wrap;
import static java.util.Arrays.asList;

class DelineationApplicationTest
{

    private final ApplicationSpy spy = new ApplicationSpy();

    @Test
    void shouldDelegateEventsToUnderlyingApplication()
    {
        // Given
        final DelineationApplication app = new DelineationApplication(spy);
        assertThat(spy.invoked()).isEmpty();

        // When
        app.onStart();
        app.onStop();
        app.work();
        app.handle(new Listen().set(99, 8808, SINGLE_BYTE));
        app.onEvent(new StartedListening(8888, 5));

        // Then
        assertEquals(
                spy.invoked(),
                "onStart",
                "onStop",
                "work",
                new StartedListening(8888, 5)
        );
    }

    @Test
    void shouldPassMessagesThrough()
    {
        final DelineationApplication app = new DelineationApplication(spy);

        // When
        MessageReceived messageReceived1 = new MessageReceived(new ConnectionIdValue(8899, 4)).set(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7}), 5);
        MessageReceived messageReceived2 = new MessageReceived(new ConnectionIdValue(6234, 5)).set(ByteBuffer.wrap(new byte[]{0}), 1);
        app.onEvent(messageReceived1);
        app.onEvent(messageReceived2);

        // Then
        assertEquals(spy.invoked(), messageReceived1, messageReceived2);
    }

    @Test
    void shouldEncodeSingleByteDataAsMessage()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(99, 8808, SINGLE_BYTE));
        app.onEvent(new StartedListening(8808, 99));
        app.onEvent(new ConnectionAccepted(8808, 51, "localhost", 33160, 5, 65536, 1313280));

        // When
        app.onEvent(new DataReceived(8808, 51, 1, 1, 30, wrap(new byte[]{5})));
        app.onEvent(new DataReceived(8809, 51, 1, 1, 30, wrap(new byte[]{6})));

        // Then
        assertEquals(
                spy.invoked(),
                new StartedListening(8808, 99),
                new ConnectionAccepted(8808, 51, "localhost", 33160, 5, 65536, 1313280),
                new MessageReceived(new ConnectionIdValue(8808, 51)).set(ByteBuffer.wrap(new byte[]{5}), 1),
                new MessageReceived(new ConnectionIdValue(8809, 51)).set(ByteBuffer.wrap(new byte[]{6}), 1)
        );
    }

    @Test
    void shouldEncodeMultipleBytesAsMultipleIndividualMessages()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(99, 8808, SINGLE_BYTE));
        app.onEvent(new StartedListening(8808, 99));
        app.onEvent(new ConnectionAccepted(8808, 100, "localhost", 33160, 5, 65536, 1313280));

        // When
        DataReceived event = new DataReceived(8808, 100, 1, 3, 30, wrap(new byte[]{1, 2, 3}));
        app.onEvent(event);

        // Then
        assertEquals(
                spy.invoked(),
                new StartedListening(8808, 99),
                new ConnectionAccepted(8808, 100, "localhost", 33160, 5, 65536, 1313280),
                new MessageReceived(event).set(ByteBuffer.wrap(new byte[]{1}), 1),
                new MessageReceived(event).set(ByteBuffer.wrap(new byte[]{2}), 1),
                new MessageReceived(event).set(ByteBuffer.wrap(new byte[]{3}), 1)
        );
    }

    @Test
    void shouldSupportMultipleConnections()
    {
        final DelineationApplication app = new DelineationApplication(spy);
        app.handle(new Listen().set(100, 8808, SINGLE_BYTE));
        app.onEvent(new StartedListening(8808, 100));
        app.onEvent(new ConnectionAccepted(8808, 100, "localhost", 33160, 8, 65536, 1313280));
        app.handle(new Listen().set(200, 8809, SINGLE_BYTE));
        app.onEvent(new StartedListening(8809, 200));
        app.onEvent(new ConnectionAccepted(8809, 200, "127.0.0.1", 33170, 9, 1234, 5678));

        // When
        app.onEvent(new DataReceived(8809, 9, 1, 1, 30, wrap(new byte[]{90})));
        app.onEvent(new DataReceived(8808, 8, 1, 1, 40, wrap(new byte[]{80})));

        // Then
        assertEquals(
                spy.messagesReceived(),
                new MessageReceived(new ConnectionIdValue(8809, 9)).set(ByteBuffer.wrap(new byte[]{90}), 1),
                new MessageReceived(new ConnectionIdValue(8808, 8)).set(ByteBuffer.wrap(new byte[]{80}), 1)

        );
    }

    private void assertEquals(final List<Object> actual, final Object... expected)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(asList(expected));
    }

    private static class ApplicationSpy implements Application
    {

        private final List<Object> invoked = new ArrayList<>();

        List<Object> invoked()
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