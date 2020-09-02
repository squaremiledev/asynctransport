package dev.squaremile.asynctcp.serialization;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.StartedListeningDecoder;
import dev.squaremile.asynctcp.testfixtures.Assertions;
import dev.squaremile.asynctcp.testfixtures.TransportEventsSpy;

class SerializingApplicationTest
{

    private static final int OFFSET = 3;
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final StartedListeningDecoder decoder = new StartedListeningDecoder();
    private final TransportEventsSpy eventsSpy = new TransportEventsSpy();

    @Test
    void shouldSerializeEvent()
    {
        // Given
        SerializingApplication application = new SerializingApplication(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(buffer, headerDecoder.encodedLength() + headerDecoder.offset(), headerDecoder.blockLength(), headerDecoder.version());
                    eventsSpy.onEvent(new StartedListening(decoder.port(), decoder.commandId()));
                }
        );

        // When
        application.onEvent(new StartedListening(8888, 5));

        // Then
        Assertions.assertEqual(eventsSpy.all(), new StartedListening(8888, 5));
    }
}