package dev.squaremile.asynctcp.serialization;

import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.domain.api.commands.Listen;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.domain.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.StartedListeningDecoder;
import dev.squaremile.asynctcp.sbe.TransportCommandFailedDecoder;

public class TransportEventDecoders
{
    private final Int2ObjectHashMap<TransportEventDecoder> eventDecoders = new Int2ObjectHashMap<>();

    public TransportEventDecoders()
    {
        final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        registerStartedListening(eventDecoders, headerDecoder);
        registerTransportCommandFailedDecoder(eventDecoders, headerDecoder);
    }

    private void registerStartedListening(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final StartedListeningDecoder decoder = new StartedListeningDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    return new StartedListening(decoder.port(), decoder.commandId());
                }
        );
    }

    private void registerTransportCommandFailedDecoder(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final TransportCommandFailedDecoder decoder = new TransportCommandFailedDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    return new TransportCommandFailed(
                            decoder.port(),
                            decoder.commandId(),
                            "some details", // TODO
                            Listen.class // TODO
                    );
                }
        );
    }

    public TransportEventDecoder eventDecoderForTemplateId(int templateId)
    {
        if (!eventDecoders.containsKey(templateId))
        {
            throw new IllegalArgumentException("Unregistered templateId " + templateId);
        }
        return eventDecoders.get(templateId);
    }
}
