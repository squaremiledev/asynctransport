package dev.squaremile.fix;

import java.util.function.Consumer;

import org.agrona.AsciiSequenceView;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.MessageHandler;


import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.TransportEventsDeserialization;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

class ReceivedFixMessagesHandler implements MessageHandler
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
    public void onMessage(final int msgTypeId, final MutableDirectBuffer buffer, final int index, final int length)
    {
        serializedEventsListener.onSerialized(buffer, index, length);
    }
}
