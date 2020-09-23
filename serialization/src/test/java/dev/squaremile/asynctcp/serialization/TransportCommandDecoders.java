package dev.squaremile.asynctcp.serialization;

import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.commands.CloseConnection;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.commands.Listen;
import dev.squaremile.asynctcp.domain.api.commands.SendData;
import dev.squaremile.asynctcp.domain.api.commands.StopListening;
import dev.squaremile.asynctcp.sbe.CloseConnectionDecoder;
import dev.squaremile.asynctcp.sbe.ConnectDecoder;
import dev.squaremile.asynctcp.sbe.ListenDecoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.sbe.SendDataDecoder;
import dev.squaremile.asynctcp.sbe.StopListeningDecoder;
import dev.squaremile.asynctcp.sbe.VarDataEncodingDecoder;

public class TransportCommandDecoders
{
    private final Int2ObjectHashMap<TransportCommandDecoder> commandDecoders = new Int2ObjectHashMap<>();

    public TransportCommandDecoders()
    {
        final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        registerCloseConnection(commandDecoders, headerDecoder);
        registerConnect(commandDecoders, headerDecoder);
        registerListen(commandDecoders, headerDecoder);
        registerStopListening(commandDecoders, headerDecoder);
        registerSendData(commandDecoders, headerDecoder);
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
                    StandardEncoding standardEncoding = StandardEncoding.valueOf(decoder.encoding());
                    String remoteHost = decoder.remoteHost();
                    return new Connect().set(remoteHost, decoder.remotePort(), decoder.commandId(), decoder.timeoutMs(), standardEncoding);
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
                    StandardEncoding standardEncoding = StandardEncoding.valueOf(decoder.encoding());
                    return new Listen().set(decoder.commandId(), decoder.port(), standardEncoding);
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

    public TransportCommandDecoder commandDecoderForTemplateId(int templateId)
    {
        if (!commandDecoders.containsKey(templateId))
        {
            throw new IllegalArgumentException("Unregistered templateId " + templateId);
        }
        return commandDecoders.get(templateId);
    }
}