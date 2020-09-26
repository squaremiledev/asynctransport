package dev.squaremile.asynctcp.serialization;

import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import dev.squaremile.asynctcp.api.app.TransportEvent;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.testfixtures.TransportEventsSpy;

import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;

class SerializingApplicationTest
{
    private static final int OFFSET = 3;
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
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
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    eventsSpy.onEvent(decoders.eventDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset));
                }
        );

        // When
        application.onEvent(event);

        // Then
        assertEqual(eventsSpy.all(), event);
    }
}