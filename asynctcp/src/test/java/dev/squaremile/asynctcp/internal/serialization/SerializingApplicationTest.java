package dev.squaremile.asynctcp.internal.serialization;

import java.util.stream.Stream;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.TransportEvent;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;
import dev.squaremile.asynctcp.fixtures.transport.TransportEventsSpy;

import static dev.squaremile.asynctcp.fixtures.transport.Assertions.assertEqual;

class SerializingApplicationTest
{
    private static final int OFFSET = 4;
    private final TransportEventsSpy eventsSpy = new TransportEventsSpy();
    private final TransportEventDecoders decoders = new TransportEventDecoders();


    static Stream<TransportEvent> serializableEvents()
    {
        return Fixtures.oneToOneSerializableEvents();
    }

    @ParameterizedTest
    @MethodSource("serializableEvents")
    void shouldSerializeEvent(final TransportEvent event)
    {
        // Given
        SerializingApplication application = new SerializingApplication(
                new ExpandableArrayBuffer(64),
                OFFSET,
                (buffer, offset, length) -> eventsSpy.onEvent(decoders.decode(buffer, offset, length))
        );

        // When
        application.onEvent(event);

        // Then
        assertEqual(eventsSpy.all(), event);
    }

    @Test
    void shouldDeserializeMessageReceivedEvent()
    {
        byte[] initialContent = new byte[10];
        MessageReceived event = new MessageReceived(new ConnectionIdValue(8899, 4)).set(new UnsafeBuffer(new byte[]{1, 2, 3, 4, 5, 6, 7}), 1, 5);
        event.buffer().getBytes(event.offset(), initialContent, 0, event.length());

        // Given
        SerializingApplication application = new SerializingApplication(
                new ExpandableArrayBuffer(64),
                OFFSET,
                (buffer, offset, length) -> eventsSpy.onEvent(decoders.decode(buffer, offset, length))
        );

        // When
        application.onEvent(event);

        // Then
        assertThat(eventsSpy.all().size()).isEqualTo(1);
        MessageReceived deserializedEvent = (MessageReceived)eventsSpy.all().get(0);
        assertThat(deserializedEvent.connectionId()).isEqualTo(4);
        assertThat(deserializedEvent.port()).isEqualTo(8899);

        byte[] deserializedContent = new byte[10];
        deserializedEvent.buffer().getBytes(deserializedEvent.offset(), deserializedContent, 0, deserializedEvent.length());
        assertThat(deserializedContent).isEqualTo(initialContent);
    }
}