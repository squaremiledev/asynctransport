package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.MessageListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;
import dev.squaremile.asynctcp.testfitures.CapturedItems;
import dev.squaremile.asynctcp.testfitures.EventsSpy;

public final class MessageReceivedSpy extends EventsSpy<MessageReceived> implements MessageListener
{
    private final CapturedItems<MessageReceived> items;

    public MessageReceivedSpy()
    {
        this(new CapturedItems<>());
    }

    private MessageReceivedSpy(final CapturedItems<MessageReceived> items)
    {
        super(items);
        this.items = items;
    }

    @Override
    public void onMessage(final MessageReceived messageReceived)
    {
        items.add(messageReceived.copy());
    }
}
