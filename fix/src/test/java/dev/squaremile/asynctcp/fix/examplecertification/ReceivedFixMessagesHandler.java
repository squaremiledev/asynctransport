package dev.squaremile.asynctcp.fix.examplecertification;

import java.util.function.Consumer;

import org.agrona.AsciiSequenceView;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.MessageHandler;


import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.internal.serialization.TransportEventsDeserialization;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;

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
    public void onMessage(final int msgTypeId, final MutableDirectBuffer buffer, final int index, final int length)
    {
        serializedEventsListener.onSerialized(buffer, index, length);
    }
}
