package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;
import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.sbe.ConnectedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionAcceptedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionClosedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionCommandFailedDecoder;
import dev.squaremile.asynctcp.sbe.ConnectionResetByPeerDecoder;
import dev.squaremile.asynctcp.sbe.DataSentDecoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.MessageReceivedDecoder;
import dev.squaremile.asynctcp.sbe.StartedListeningDecoder;
import dev.squaremile.asynctcp.sbe.StoppedListeningDecoder;
import dev.squaremile.asynctcp.sbe.TransportCommandFailedDecoder;
import dev.squaremile.asynctcp.sbe.VarDataEncodingDecoder;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionCommandFailed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

// TODO [perf]: avoid garbage
public class TransportEventDecoders
{
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final Int2ObjectHashMap<TransportEventDecoder> eventDecoders = new Int2ObjectHashMap<>();
    private int decodedLength;

    public TransportEventDecoders()
    {
        registerConnectedDecoder(eventDecoders, headerDecoder);
        registerConnectionAcceptedDecoder(eventDecoders, headerDecoder);
        registerConnectionClosed(eventDecoders, headerDecoder);
        registerConnectionCommandFailedDecoder(eventDecoders, headerDecoder);
        registerConnectionResetByPeer(eventDecoders, headerDecoder);
        registerDataSent(eventDecoders, headerDecoder);
        registerMessageReceived(eventDecoders, headerDecoder);
        registerStartedListening(eventDecoders, headerDecoder);
        registerStoppedListening(eventDecoders, headerDecoder);
        registerTransportCommandFailedDecoder(eventDecoders, headerDecoder);
    }

    public TransportEvent decode(final DirectBuffer buffer, final int offset, final int length)
    {
        decodedLength = 0;
        headerDecoder.wrap(buffer, offset);
        TransportEvent result = eventDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset, length);
        if (decodedLength != length)
        {
            throw new IllegalArgumentException("Decoded length of " + decodedLength + " does not match declared length of " + length);
        }
        return result;
    }

    private void registerStoppedListening(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final StoppedListeningDecoder decoder = new StoppedListeningDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    StoppedListening result = new StoppedListening(decoder.port(), decoder.commandId());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerConnectedDecoder(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectedDecoder decoder = new ConnectedDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
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
                    Connected result = new Connected(
                            decoder.port(),
                            decoder.commandId(),
                            decoder.remoteHost(),
                            decoder.remotePort(),
                            decoder.connectionId(),
                            decoder.inboundPduLimit(),
                            decoder.outboundPduLimit()
                    );
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerConnectionAcceptedDecoder(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectionAcceptedDecoder decoder = new ConnectionAcceptedDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
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
                    ConnectionAccepted result = new ConnectionAccepted(
                            decoder.port(),
                            decoder.commandId(),
                            decoder.remoteHost(),
                            decoder.remotePort(),
                            decoder.connectionId(),
                            decoder.inboundPduLimit(),
                            decoder.outboundPduLimit()
                    );
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerConnectionClosed(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectionClosedDecoder decoder = new ConnectionClosedDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    ConnectionClosed result = new ConnectionClosed(decoder.port(), decoder.connectionId(), decoder.commandId());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerConnectionCommandFailedDecoder(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectionCommandFailedDecoder decoder = new ConnectionCommandFailedDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    ConnectionCommandFailed result = new ConnectionCommandFailed(
                            decoder.port(),
                            decoder.commandId(),
                            decoder.details(),
                            decoder.connectionId()
                    );
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerConnectionResetByPeer(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectionResetByPeerDecoder decoder = new ConnectionResetByPeerDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    ConnectionResetByPeer result = new ConnectionResetByPeer(decoder.port(), decoder.connectionId(), decoder.commandId());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerDataSent(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final DataSentDecoder decoder = new DataSentDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    DataSent result = new DataSent(decoder.port(), decoder.connectionId(), decoder.bytesSent(), decoder.totalBytesSent(), decoder.totalBytesBuffered(), decoder.commandId());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerStartedListening(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final StartedListeningDecoder decoder = new StartedListeningDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    StartedListening result = new StartedListening(decoder.port(), decoder.commandId());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerMessageReceived(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final MessageReceivedDecoder decoder = new MessageReceivedDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    VarDataEncodingDecoder srcData = decoder.data();
                    DirectBuffer dataBuffer = srcData.buffer();
                    int dataOffset = srcData.offset() + VarDataEncodingDecoder.lengthEncodingLength();
                    int dataLength = (int)srcData.length();
                    MessageReceived result = new MessageReceived(new ConnectionIdValue(decoder.port(), decoder.connectionId()))
                            .set(dataBuffer, dataOffset, dataLength);
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerTransportCommandFailedDecoder(final Int2ObjectHashMap<TransportEventDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final TransportCommandFailedDecoder decoder = new TransportCommandFailedDecoder();
        eventDecoders.put(
                decoder.sbeTemplateId(), (buffer, offset, length) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    decoder.wrap(
                            buffer,
                            headerDecoder.encodedLength() + headerDecoder.offset(),
                            headerDecoder.blockLength(),
                            headerDecoder.version()
                    );
                    TransportCommandFailed result = new TransportCommandFailed(
                            decoder.port(),
                            decoder.commandId(),
                            decoder.details(),
                            decoder.commandType()
                    );
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private TransportEventDecoder eventDecoderForTemplateId(int templateId)
    {
        if (!eventDecoders.containsKey(templateId))
        {
            throw new IllegalArgumentException("Unregistered templateId " + templateId);
        }
        return eventDecoders.get(templateId);
    }
}
