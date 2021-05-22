package dev.squaremile.asynctcp.fix.examplecertification;

import java.util.function.Consumer;

import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.internal.serialization.TransportEventsDeserialization;
import dev.squaremile.asynctcp.internal.serialization.messaging.MessageHandler;

public class ReceivedFixMessagesHandler implements MessageHandler
{
    private final AsciiSequenceView content = new AsciiSequenceView();
    private final SerializedEventListener serializedEventsListener;

    public ReceivedFixMessagesHandler(final Consumer<CharSequence> fixMessages)
    {
        serializedEventsListener = new TransportEventsDeserialization(
                event ->
                {
                    if (event instanceof MessageReceived)
                    {
                        MessageReceived message = (MessageReceived)event;
                        fixMessages.accept(content.wrap(message.buffer(), message.offset(), message.length()));
                    }
                });
    }

    @Override
    public void onMessage(final DirectBuffer buffer, final int offset, final int length)
    {
        serializedEventsListener.onSerialized(buffer, offset, length);
    }
}
