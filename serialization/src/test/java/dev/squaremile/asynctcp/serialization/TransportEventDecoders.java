package dev.squaremile.asynctcp.serialization;

import java.nio.ByteBuffer;

import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.domain.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.domain.api.events.DataSent;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.domain.api.events.StoppedListening;
import dev.squaremile.asynctcp.domain.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.sbe.ConnectedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionAcceptedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionClosedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionResetByPeerDecoder;
import dev.squaremile.asynctcp.sbe.DataSentDecoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.MessageReceivedDecoder;
import dev.squaremile.asynctcp.sbe.StartedListeningDecoder;
import dev.squaremile.asynctcp.sbe.StoppedListeningDecoder;
import dev.squaremile.asynctcp.sbe.TransportCommandFailedDecoder;
import dev.squaremile.asynctcp.sbe.VarDataEncodingDecoder;

public class TransportEventDecoders
{
    private final Int2ObjectHashMap<TransportEventDecoder> eventDecoders = new Int2ObjectHashMap<>();

    public TransportEventDecoders()
    {
        final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        registerConnectedDecoder(eventDecoders, headerDecoder);
        registerConnectionAcceptedDecoder(eventDecoders, headerDecoder);
        registerConnectionClosed(eventDecoders, headerDecoder);
        registerConnectionResetByPeer(eventDecoders, headerDecoder);
        registerDataSent(eventDecoders, headerDecoder);
        registerMessageReceived(eventDecoders, headerDecoder);
        registerStartedListening(eventDecoders, headerDecoder);
        registerStoppedListening(eventDecoders, headerDecoder);
        registerTransportCommandFailedDecoder(eventDecoders, headerDecoder);
    }

    private void registerStoppedListening(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final StoppedListeningDecoder decoder = new StoppedListeningDecoder();
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
                    return new StoppedListening(decoder.port(), decoder.commandId());
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

    private void registerDataSent(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final DataSentDecoder decoder = new DataSentDecoder();
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
                    return new DataSent(decoder.port(), decoder.connectionId(), decoder.bytesSent(), decoder.totalBytesSent(), decoder.totalBytesBuffered(), decoder.commandId());
                }
        );
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

    private void registerMessageReceived(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final MessageReceivedDecoder decoder = new MessageReceivedDecoder();
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
                    VarDataEncodingDecoder srcData = decoder.data();
                    byte[] dstArray = new byte[(int)srcData.length()];
                    srcData.buffer().getBytes(srcData.offset() + srcData.encodedLength(), dstArray);
                    return new MessageReceived(new ConnectionIdValue(decoder.port(), decoder.connectionId())).set(ByteBuffer.wrap(dstArray), dstArray.length);
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

    public TransportEventDecoder eventDecoderForTemplateId(int templateId)
    {
        if (!eventDecoders.containsKey(templateId))
        {
            throw new IllegalArgumentException("Unregistered templateId " + templateId);
        }
        return eventDecoders.get(templateId);
    }
}
