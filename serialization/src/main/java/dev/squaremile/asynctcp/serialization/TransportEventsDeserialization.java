package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.app.TransportEventsListener;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;

class TransportEventsDeserialization implements SerializedEventListener
{
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final TransportEventDecoders decoders = new TransportEventDecoders();
    private final TransportEventsListener transportEventsListener;

    TransportEventsDeserialization(final TransportEventsListener transportEventsListener)
    {
        this.transportEventsListener = transportEventsListener;
    }

    @Override
    public void onSerializedEvent(final DirectBuffer buffer, final int offset)
    {
        headerDecoder.wrap(buffer, offset);
        transportEventsListener.onEvent(decoders.eventDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset));
    }
}
