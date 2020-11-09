package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableRingBuffer;


import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.serialization.internal.SerializedMessageListener;
import dev.squaremile.asynctcp.serialization.internal.TransportCommandDecoders;
import dev.squaremile.asynctcp.serialization.internal.TransportEventDecoders;
import dev.squaremile.asynctcp.transport.testfixtures.CommandsProvidingTransport;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;

public class PrintingMessageListener implements SerializedMessageListener
{
    private final TransportEventDecoders transportEventDecoders = new TransportEventDecoders();
    private final TransportCommandDecoders transportCommandDecoders = new TransportCommandDecoders(new CommandsProvidingTransport(1234, rawStreaming()));
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final ExpandableRingBuffer applicationMessagesLog = new ExpandableRingBuffer();
    private final StringBuilder humanReadableLog = new StringBuilder();
    private final SerializedMessageListener messageListener = (buffer, sourceOffset, length) ->
    {
        humanReadableLog.append(decode(buffer, sourceOffset, length));
        humanReadableLog.append("\n");
    };

    private Object decode(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        headerDecoder.wrap(sourceBuffer, sourceOffset);
        if (headerDecoder.templateId() >= 100)
        {
            return transportCommandDecoders.decode(sourceBuffer, sourceOffset, length);
        }
        else
        {
            return transportEventDecoders.decode(sourceBuffer, sourceOffset, length);
        }
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        applicationMessagesLog.append(sourceBuffer, sourceOffset, length);
    }

    public String logContent()
    {
        humanReadableLog.setLength(0);
        applicationMessagesLog.forEach(
                (buffer, offset, length, headOffset) ->
                {
                    messageListener.onSerialized(buffer, offset, length);
                    return true;
                },
                Integer.MAX_VALUE
        );
        String content = humanReadableLog.toString();
        humanReadableLog.setLength(0);
        return content;
    }
}
