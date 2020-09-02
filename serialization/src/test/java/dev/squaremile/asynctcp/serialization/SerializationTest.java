package dev.squaremile.asynctcp.serialization;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderEncoder;
import dev.squaremile.asynctcp.sbe.StartedListeningDecoder;
import dev.squaremile.asynctcp.sbe.StartedListeningEncoder;

class SerializationTest
{

    @Test
    void shouldSerialize()
    {
        MutableDirectBuffer buffer = new UnsafeBuffer(new byte[100]);
        StartedListening eventToSerialize = new StartedListening(8888, 5);
        MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
        MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        StartedListeningEncoder encoder = new StartedListeningEncoder();
        StartedListeningDecoder decoder = new StartedListeningDecoder();
        encoder.wrapAndApplyHeader(buffer, 3, messageHeaderEncoder)
                .port(eventToSerialize.port())
                .commandId(eventToSerialize.commandId());

        // When
        messageHeaderDecoder.wrap(buffer, 3);
        decoder.wrap(buffer, messageHeaderDecoder.encodedLength() + messageHeaderDecoder.offset(), messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
        StartedListening deserializedEvent = new StartedListening(decoder.port(), decoder.commandId());

        // Then
        assertThat(deserializedEvent).usingRecursiveComparison().isEqualTo(eventToSerialize);


    }
}