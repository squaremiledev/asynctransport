package dev.squaremile.asynctcp.serialization.internal;

import java.util.stream.Stream;

import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.testfixtures.TransportEventsSpy;

import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;

class SerializingApplicationTest
{
    private static final int OFFSET = 4;
    private final TransportEventsSpy eventsSpy = new TransportEventsSpy();
    private final TransportEventDecoders decoders = new TransportEventDecoders();


    static Stream<TransportEvent> serializableEvents()
    {
        return Fixtures.serializableEvents();
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
}