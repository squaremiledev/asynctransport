package dev.squaremile.asynctcp.encodings;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;


import dev.squaremile.asynctcp.domain.api.events.MessageListener;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;
import dev.squaremile.asynctcp.testfixtures.CapturedItems;
import dev.squaremile.asynctcp.testfixtures.Spy;

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
                    ByteBuffer target = ByteBuffer.wrap(data);
                    msg.copyDataTo(target);
                    return data;
                }).collect(Collectors.toList());
    }
}
