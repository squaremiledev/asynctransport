package dev.squaremile.asynctcp.fixtures;


import java.util.function.BiConsumer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

import static java.util.Objects.requireNonNull;

public class ResponseApplication implements ConnectionApplication
{
    private final ConnectionTransport transport;
    private final EventListener eventListener;
    private final BiConsumer<MessageReceived, SendMessage> response;

    public interface ByteConverter
    {
        byte convert(byte value);
    }

    public ResponseApplication(final ConnectionTransport transport, final EventListener eventListener, final ByteConverter byteConverter)
    {
        this(transport, eventListener, (messageReceived, outSendMessage) ->
        {
            for (int i = 0; i < messageReceived.length(); i++)
            {
                outSendMessage.prepare().putByte(outSendMessage.offset() + i, byteConverter.convert(messageReceived.buffer().getByte(messageReceived.offset() + i)));
            }
            outSendMessage.commit(messageReceived.length());
        });
    }

    public ResponseApplication(final ConnectionTransport transport, final EventListener eventListener, final BiConsumer<MessageReceived, SendMessage> response)
    {
        this.transport = requireNonNull(transport);
        this.eventListener = eventListener;
        this.response = response;
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
            SendMessage sendMessage = transport.command(SendMessage.class);
            response.accept((MessageReceived)event, sendMessage);
            transport.handle(sendMessage);
        }
    }
}
