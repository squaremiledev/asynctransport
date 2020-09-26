package dev.squaremile.asynctcp.serialization;

import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import dev.squaremile.asynctcp.api.app.TransportEvent;
import dev.squaremile.asynctcp.testfixtures.TransportEventsSpy;

import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;

class TransportEventsDeserializationTest
{
    private static final int OFFSET = 5;
    private final TransportEventsSpy eventsSpy = new TransportEventsSpy();

    static Stream<TransportEvent> serializableEvents()
    {
        return Fixtures.serializableEvents();
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

    private SerializingApplication notifyingAboutSerializedEvent(final TransportEventsDeserialization transportEventsDeserialization)
    {
        return new SerializingApplication(new UnsafeBuffer(new byte[200]), OFFSET, transportEventsDeserialization);
    }
}
