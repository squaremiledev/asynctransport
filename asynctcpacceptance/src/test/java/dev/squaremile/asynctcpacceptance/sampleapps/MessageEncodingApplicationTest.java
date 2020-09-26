package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Event;
import dev.squaremile.asynctcp.api.events.DataReceived;
import dev.squaremile.asynctcp.api.events.MessageReceived;
import dev.squaremile.asynctcp.api.events.StartedListening;
import dev.squaremile.asynctcp.api.values.ConnectionIdValue;

import static dev.squaremile.asynctcp.api.values.PredefinedTransportEncoding.SINGLE_BYTE;
import static java.nio.ByteBuffer.wrap;
import static java.util.Arrays.asList;

class MessageEncodingApplicationTest
{

    private final ApplicationSpy spy = new ApplicationSpy();
    private final MessageEncodingApplication app = new MessageEncodingApplication(spy, SINGLE_BYTE);

    @Test
    void shouldDelegateToUnderlyingApplication()
    {
        // Given
        assertThat(spy.invoked()).isEmpty();

        // When
        app.onStart();
        app.onStop();
        app.work();
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

    private void assertEquals(final List<Object> actual, final Object... expected)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(asList(expected));
    }

    @Test
    void shouldPassMessagesThrough()
    {
        // When
        MessageReceived messageReceived1 = new MessageReceived(new ConnectionIdValue(8899, 4)).set(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7}), 5);
        MessageReceived messageReceived2 = new MessageReceived(new ConnectionIdValue(6234, 5)).set(ByteBuffer.wrap(new byte[]{0}), 1);
        app.onEvent(messageReceived1);
        app.onEvent(messageReceived2);

        // Then
        assertEquals(spy.invoked(), messageReceived1, messageReceived2);
    }

    @Test
    void shouldEncodeDataAsMessage()
    {
        // When
        app.onEvent(new DataReceived(8808, 100, 1, 1, 30, wrap(new byte[]{5})));

        // Then
        assertEquals(spy.invoked(), new MessageReceived(new ConnectionIdValue(8808, 100)).set(ByteBuffer.wrap(new byte[]{5}), 1));
    }

    private static class ApplicationSpy implements Application
    {
        private final List<Object> invoked = new ArrayList<>();

        List<Object> invoked()
        {
            return invoked;
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
            invoked.add(event);
        }
    }
}