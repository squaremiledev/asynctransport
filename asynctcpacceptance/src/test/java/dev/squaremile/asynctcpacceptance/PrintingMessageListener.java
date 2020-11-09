package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableRingBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;


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
    private final RingBuffer logBuffer = new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 * 1024 + TRAILER_LENGTH]));
    private final ExpandableRingBuffer expandableRingBuffer = new ExpandableRingBuffer();
    private final StringBuilder log = new StringBuilder();
    private final SerializedMessageListener messageListener = (buffer, sourceOffset, length) ->
    {
        log.append(decode(buffer, sourceOffset, length));
        log.append("\n");
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
        logBuffer.write(1, sourceBuffer, sourceOffset, length);
        expandableRingBuffer.append(sourceBuffer, sourceOffset, length);
    }

    public String logContent()
    {
        log.setLength(0);
        expandableRingBuffer.forEach(
                (buffer, offset, length, headOffset) ->
                {
                    messageListener.onSerialized(buffer, offset, length);
                    return true;
                },
                Integer.MAX_VALUE
        );
        String content = log.toString();
        log.setLength(0);
        return content;
    }
}
