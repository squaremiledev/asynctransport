package dev.squaremile.asynctcp.serialization;

import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.domain.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.domain.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.sbe.ConnectedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionAcceptedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionClosedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionResetByPeerDecoder;
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
        registerConnectedDecoder(eventDecoders, headerDecoder);
        registerConnectionAcceptedDecoder(eventDecoders, headerDecoder);
        registerConnectionClosed(eventDecoders, headerDecoder);
        registerConnectionResetByPeer(eventDecoders, headerDecoder);
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
                            decoder.details(),
                            decoder.commandType()
                    );
                }
        );
    }

    private void registerConnectedDecoder(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectedDecoder decoder = new ConnectedDecoder();
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
                    // got away with out of order variable length decoding as it't the only field of its type
                    // and only ordering within variable length fields matter
                    return new Connected(
                            decoder.port(),
                            decoder.commandId(),
                            decoder.remoteHost(),
                            decoder.remotePort(),
                            decoder.connectionId(),
                            decoder.inboundPduLimit(),
                            decoder.outboundPduLimit()
                    );
                }
        );
    }

    private void registerConnectionAcceptedDecoder(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectionAcceptedDecoder decoder = new ConnectionAcceptedDecoder();
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
                    // got away with out of order variable length decoding as it't the only field of its type
                    // and only ordering within variable length fields matter
                    return new ConnectionAccepted(
                            decoder.port(),
                            decoder.commandId(),
                            decoder.remoteHost(),
                            decoder.remotePort(),
                            decoder.connectionId(),
                            decoder.inboundPduLimit(),
                            decoder.outboundPduLimit()
                    );
                }
        );
    }

    private void registerConnectionClosed(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectionClosedDecoder decoder = new ConnectionClosedDecoder();
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
                    return new ConnectionClosed(decoder.port(), decoder.connectionId(), decoder.commandId());
                }
        );
    }

    private void registerConnectionResetByPeer(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectionResetByPeerDecoder decoder = new ConnectionResetByPeerDecoder();
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
                    return new ConnectionResetByPeer(decoder.port(), decoder.connectionId(), decoder.commandId());
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
