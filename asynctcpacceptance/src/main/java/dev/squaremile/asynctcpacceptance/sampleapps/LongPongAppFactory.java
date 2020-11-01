package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.function.LongConsumer;


import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

class LongPongAppFactory implements ApplicationFactory
{
    private final EventListener pongSpy;
    private final int port;
    private final LongConsumer messageListener;
    private final Delineation delineation;

    public LongPongAppFactory(final Delineation delineation, final int port, final EventListener pongSpy, final LongConsumer messageListener)
    {
        this.pongSpy = pongSpy;
        this.port = port;
        this.messageListener = messageListener;
        this.delineation = delineation;
    }

    @Override
    public EventDrivenApplication create(final Transport transport)
    {
        return new EventDrivenApplication()
        {
            private static final int CONNECT_COMMAND_ID = 1;

            @Override
            public void onStart()
            {
                connect();
            }

            @Override
            public void onEvent(final Event event)
            {
                pongSpy.onEvent(event);
                if (event instanceof MessageReceived)
                {
                    MessageReceived messageReceived = (MessageReceived)event;
                    long receivedNumber = messageReceived.buffer().getLong(messageReceived.offset());
                    long newNumber = receivedNumber + 1;
                    SendMessage sendMessage = transport.command(messageReceived.connectionId(), SendMessage.class);
                    sendMessage.prepare(8).putLong(sendMessage.offset(), newNumber);
                    sendMessage.commit();
                    transport.handle(sendMessage);

                    messageListener.accept(newNumber);
                }
            }

            private void connect()
            {
                transport.handle(transport.command(Connect.class).set("localhost", port, CONNECT_COMMAND_ID, 100, delineation));
            }
        };
    }
}
