package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.app.TransportCommandHandler;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;

class TransportCommandDeserialization implements SerializedCommandListener
{
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final TransportCommandDecoders decoders = new TransportCommandDecoders();
    private final TransportCommandHandler transportCommandHandler;

    public TransportCommandDeserialization(final TransportCommandHandler transportCommandHandler)
    {
        this.transportCommandHandler = transportCommandHandler;
    }

    @Override
    public void onSerializedCommand(final DirectBuffer buffer, final int offset)
    {
        headerDecoder.wrap(buffer, offset);
        transportCommandHandler.handle(decoders.commandDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset));
    }
}
