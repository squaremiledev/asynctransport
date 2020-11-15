package dev.squaremile.asynctcp.fixtures;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

import static java.util.Objects.requireNonNull;

public class MessageEchoApplication implements ConnectionApplication
{
    private final ConnectionTransport transport;
    private final EventListener eventListener;

    public MessageEchoApplication(final ConnectionTransport transport, final EventListener eventListener)
    {
        this.transport = requireNonNull(transport);
        this.eventListener = eventListener;
    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onStop()
    {
    }

    @Override
    public void work()
    {

    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        eventListener.onEvent(event);
        if (event instanceof MessageReceived)
        {
            transport.handle(sendMessageCommandWithDataFrom((MessageReceived)event));
        }
    }

    private SendMessage sendMessageCommandWithDataFrom(final MessageReceived messageReceivedEvent)
    {
        SendMessage sendMessage = transport.command(SendMessage.class);
        messageReceivedEvent.buffer().getBytes(messageReceivedEvent.offset(), sendMessage.prepare(messageReceivedEvent.length()), sendMessage.offset(), messageReceivedEvent.length());
        sendMessage.commit();
        return sendMessage;
    }
}
