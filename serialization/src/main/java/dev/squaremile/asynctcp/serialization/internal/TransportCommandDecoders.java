package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;
import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.sbe.CloseConnectionDecoder;
import dev.squaremile.asynctcp.sbe.ConnectDecoder;
import dev.squaremile.asynctcp.sbe.ListenDecoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.SendDataDecoder;
import dev.squaremile.asynctcp.sbe.SendMessageDecoder;
import dev.squaremile.asynctcp.sbe.StopListeningDecoder;
import dev.squaremile.asynctcp.sbe.VarDataEncodingDecoder;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

// TODO [perf]: avoid garbage
public class TransportCommandDecoders
{
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final Int2ObjectHashMap<TransportCommandDecoder> commandDecoders = new Int2ObjectHashMap<>();
    private final TransportOnDuty transport;
    private int decodedLength;

    public TransportCommandDecoders(final TransportOnDuty transport)
    {
        this.transport = transport;
        registerCloseConnection(commandDecoders, headerDecoder);
        registerConnect(commandDecoders, headerDecoder);
        registerListen(commandDecoders, headerDecoder);
        registerStopListening(commandDecoders, headerDecoder);
        registerSendData(commandDecoders, headerDecoder);
        registerSendMessage(commandDecoders, headerDecoder);
    }

    public TransportCommand decode(DirectBuffer buffer, int offset, final int length)
    {
        decodedLength = 0;
        headerDecoder.wrap(buffer, offset);
        TransportCommand result = commandDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset);
        if (decodedLength != length)
        {
            throw new IllegalArgumentException("Decoded length of " + decodedLength + " does not match declared length of " + length);
        }
        return result;
    }

    private void registerCloseConnection(final Int2ObjectHashMap<TransportCommandDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final CloseConnectionDecoder decoder = new CloseConnectionDecoder();
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
                    CloseConnection result = new CloseConnection(new ConnectionIdValue(decoder.port(), decoder.connectionId())).set(decoder.commandId());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerConnect(final Int2ObjectHashMap<TransportCommandDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ConnectDecoder decoder = new ConnectDecoder();
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
                    Delineation delineation = new Delineation(DelineationTypeMapping.toDomain(decoder.delineationType()), decoder.delineationKnownLength(), decoder.delineationPattern());
                    String remoteHost = decoder.remoteHost();
                    Connect result = new Connect().set(remoteHost, decoder.remotePort(), decoder.commandId(), decoder.timeoutMs(), delineation);
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerListen(final Int2ObjectHashMap<TransportCommandDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final ListenDecoder decoder = new ListenDecoder();
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
                    Listen result = new Listen().set(
                            decoder.commandId(),
                            decoder.port(),
                            new Delineation(DelineationTypeMapping.toDomain(decoder.delineationType()), decoder.delineationKnownLength(), decoder.delineationPattern())
                    );
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerStopListening(final Int2ObjectHashMap<TransportCommandDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final StopListeningDecoder decoder = new StopListeningDecoder();
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
                    StopListening result = new StopListening().set(decoder.commandId(), decoder.port());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerSendData(final Int2ObjectHashMap<TransportCommandDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final SendDataDecoder decoder = new SendDataDecoder();
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
                    SendData result = new SendData(decoder.port(), decoder.connectionId(), decoder.capacity()).set(dstArray, decoder.commandId());
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private void registerSendMessage(final Int2ObjectHashMap<TransportCommandDecoder> eventDecoders, final MessageHeaderDecoder headerDecoder)
    {
        final SendMessageDecoder decoder = new SendMessageDecoder();
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
                    int dataLength = (int)decoder.data().length();
                    SendMessage result = transport.command(new ConnectionIdValue(decoder.port(), decoder.connectionId()), SendMessage.class);
                    result.prepare().putBytes(result.offset(), decoder.data().buffer(), decoder.data().offset() + decoder.data().encodedLength(), dataLength);
                    result.commit(dataLength);
                    this.decodedLength = headerDecoder.encodedLength() + decoder.encodedLength();
                    return result;
                }
        );
    }

    private TransportCommandDecoder commandDecoderForTemplateId(int templateId)
    {
        if (!commandDecoders.containsKey(templateId))
        {
            throw new IllegalArgumentException("Unregistered templateId " + templateId);
        }
        return commandDecoders.get(templateId);
    }
}
