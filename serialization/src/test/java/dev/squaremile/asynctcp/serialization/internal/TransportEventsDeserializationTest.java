package dev.squaremile.asynctcp.serialization.internal;

import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.testfixtures.TransportEventsSpy;

import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;

class TransportEventsDeserializationTest
{
    private static final int OFFSET = 5;
    private final TransportEventsSpy eventsSpy = new TransportEventsSpy();

    static Stream<TransportEvent> serializableEvents()
    {
        return Fixtures.oneToOneSerializableEvents();
    }

    @ParameterizedTest
    @MethodSource("serializableEvents")
    void shouldDeserializeEvent(final TransportEvent event)
    {
        // When
        notifyingAboutSerializedEvent(new TransportEventsDeserialization(eventsSpy)).onEvent(event);

        // Then
        assertEqual(eventsSpy.all(), event);
    }

    @Test
    void shouldDeserializeMessageReceivedEvent()
    {
        byte[] initialContent = new byte[10];
        MessageReceived event = new MessageReceived(new ConnectionIdValue(8899, 4)).set(new UnsafeBuffer(new byte[]{1, 2, 3, 4, 5, 6, 7}), 1, 5);
        event.buffer().getBytes(event.offset(), initialContent, 0, event.length());
        // When
        notifyingAboutSerializedEvent(new TransportEventsDeserialization(eventsSpy)).onEvent(event);

        // Then
        assertThat(eventsSpy.all().size()).isEqualTo(1);
        MessageReceived deserializedEvent = (MessageReceived)eventsSpy.all().get(0);
        assertThat(deserializedEvent.connectionId()).isEqualTo(4);
        assertThat(deserializedEvent.port()).isEqualTo(8899);

        byte[] deserializedContent = new byte[10];
        deserializedEvent.buffer().getBytes(deserializedEvent.offset(), deserializedContent, 0, deserializedEvent.length());
        assertThat(deserializedContent).isEqualTo(initialContent);
    }

    private SerializingApplication notifyingAboutSerializedEvent(final TransportEventsDeserialization transportEventsDeserialization)
    {
        return new SerializingApplication(new UnsafeBuffer(new byte[200]), OFFSET, transportEventsDeserialization);
    }
}
