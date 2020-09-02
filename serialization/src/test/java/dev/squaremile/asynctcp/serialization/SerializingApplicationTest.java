package dev.squaremile.asynctcp.serialization;

import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import dev.squaremile.asynctcp.domain.api.commands.Listen;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.domain.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.domain.api.events.TransportEvent;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.testfixtures.TransportEventsSpy;

import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;

class SerializingApplicationTest
{

    private static final int OFFSET = 3;
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final TransportEventsSpy eventsSpy = new TransportEventsSpy();
    private final TransportEventDecoders decoders = new TransportEventDecoders();

    static Stream<TransportEvent> events()
    {
        return Stream.of(
                new StartedListening(8888, 5),
                new TransportCommandFailed(8001, 101L, "some details", Listen.class)
        );
    }

    @ParameterizedTest
    @MethodSource("events")
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