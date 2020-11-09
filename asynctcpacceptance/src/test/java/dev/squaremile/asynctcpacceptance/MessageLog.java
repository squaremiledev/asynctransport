package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableRingBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;


import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.serialization.internal.SerializedMessageListener;
import dev.squaremile.asynctcp.serialization.internal.TransportCommandDecoders;
import dev.squaremile.asynctcp.serialization.internal.TransportEventDecoders;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedEventSupplier;
import dev.squaremile.asynctcp.transport.testfixtures.CommandsProvidingTransport;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;

public class MessageLog implements SerializedMessageListener
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
        if (transportCommandDecoders.supports(headerDecoder))
        {
            return transportCommandDecoders.decode(sourceBuffer, sourceOffset, length);
        }
        else if (transportEventDecoders.supports(headerDecoder))
        {
            return transportEventDecoders.decode(sourceBuffer, sourceOffset, length);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported message " + headerDecoder);
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
        final String content = humanReadableLog.toString();
        humanReadableLog.setLength(0);
        return content;
    }

    public SerializedEventSupplier singleEventSupplier()
    {
        final OneToOneRingBuffer eventsToConsume = new OneToOneRingBuffer(new UnsafeBuffer(new byte[Math.min(1024 * 1024, applicationMessagesLog.capacity()) + TRAILER_LENGTH]));
        applicationMessagesLog.forEach(
                (buffer, offset, length, headOffset) ->
                {
                    if (transportEventDecoders.supports(headerDecoder.wrap(buffer, offset)))
                    {
                        eventsToConsume.write(1, buffer, offset, length);
                    }
                    return true;
                },
                Integer.MAX_VALUE
        );
        return eventsToConsume::read;
    }
}
