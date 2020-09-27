package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;
import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.api.app.TransportCommand;
import dev.squaremile.asynctcp.api.commands.CloseConnection;
import dev.squaremile.asynctcp.api.commands.Connect;
import dev.squaremile.asynctcp.api.commands.Listen;
import dev.squaremile.asynctcp.api.commands.SendData;
import dev.squaremile.asynctcp.api.commands.StopListening;
import dev.squaremile.asynctcp.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.values.PredefinedTransportEncoding;
import dev.squaremile.asynctcp.sbe.CloseConnectionDecoder;
import dev.squaremile.asynctcp.sbe.ConnectDecoder;
import dev.squaremile.asynctcp.sbe.ListenDecoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.SendDataDecoder;
import dev.squaremile.asynctcp.sbe.StopListeningDecoder;
import dev.squaremile.asynctcp.sbe.VarDataEncodingDecoder;

// TODO [perf]: avoid garbage
public class TransportCommandDecoders
{
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final Int2ObjectHashMap<TransportCommandDecoder> commandDecoders = new Int2ObjectHashMap<>();

    public TransportCommandDecoders()
    {
        registerCloseConnection(commandDecoders, headerDecoder);
        registerConnect(commandDecoders, headerDecoder);
        registerListen(commandDecoders, headerDecoder);
        registerStopListening(commandDecoders, headerDecoder);
        registerSendData(commandDecoders, headerDecoder);
    }

    public TransportCommand decode(DirectBuffer buffer, int offset)
    {
        headerDecoder.wrap(buffer, offset);
        return commandDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset);
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
                    return new CloseConnection(new ConnectionIdValue(decoder.port(), decoder.connectionId())).set(decoder.commandId());
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
                    PredefinedTransportEncoding predefinedTransportEncoding = PredefinedTransportEncoding.valueOf(decoder.encoding());
                    String remoteHost = decoder.remoteHost();
                    return new Connect().set(remoteHost, decoder.remotePort(), decoder.commandId(), decoder.timeoutMs(), predefinedTransportEncoding);
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
                    PredefinedTransportEncoding predefinedTransportEncoding = PredefinedTransportEncoding.valueOf(decoder.encoding());
                    return new Listen().set(decoder.commandId(), decoder.port(), predefinedTransportEncoding);
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
                    return new StopListening().set(decoder.commandId(), decoder.port());
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
                    return new SendData(decoder.port(), decoder.connectionId(), decoder.capacity()).set(dstArray, decoder.commandId());
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
