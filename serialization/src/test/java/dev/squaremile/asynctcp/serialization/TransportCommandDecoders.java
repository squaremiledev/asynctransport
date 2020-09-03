package dev.squaremile.asynctcp.serialization;

import org.agrona.collections.Int2ObjectHashMap;


import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.commands.CloseConnection;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.sbe.CloseConnectionDecoder;
import dev.squaremile.asynctcp.sbe.ConnectDecoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;

public class TransportCommandDecoders
{
    private final Int2ObjectHashMap<TransportCommandDecoder> commandDecoders = new Int2ObjectHashMap<>();

    public TransportCommandDecoders()
    {
        final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        registerCloseConnection(commandDecoders, headerDecoder);
        registerConnect(commandDecoders, headerDecoder);
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

    public TransportCommandDecoder commandDecoderForTemplateId(int templateId)
    {
        if (!commandDecoders.containsKey(templateId))
        {
            throw new IllegalArgumentException("Unregistered templateId " + templateId);
        }
        return commandDecoders.get(templateId);
    }
}
