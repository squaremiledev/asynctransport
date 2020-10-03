package dev.squaremile.asynctcp.transport.internal.transportencoding;

import java.util.List;
import java.util.stream.Collectors;


import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.testfixtures.CapturedItems;
import dev.squaremile.asynctcp.transport.testfixtures.Spy;

public final class MessageReceivedSpy extends Spy<MessageReceived> implements MessageListener
{
    private final CapturedItems<MessageReceived> items;

    MessageReceivedSpy()
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

    List<byte[]> asPdus()
    {
        return items.all().stream().map(
                msg ->
                {
                    byte[] data = new byte[msg.length()];
                    msg.buffer().getBytes(msg.offset(), data, 0, msg.length());
                    return data;
                }).collect(Collectors.toList());
    }
}
