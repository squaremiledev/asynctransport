package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.function.LongConsumer;


import dev.squaremile.asynctcp.serialization.api.delineation.FixedLengthDelineationType;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

class LongPongAppFactory implements ApplicationFactory
{
    private final EventListener pongSpy;
    private final int port;
    private final LongConsumer messageListener;

    public LongPongAppFactory(final int port, final EventListener pongSpy, final LongConsumer messageListener)
    {
        this.pongSpy = pongSpy;
        this.port = port;
        this.messageListener = messageListener;
    }

    @Override
    public Application create(final Transport transport)
    {
        return new Application()
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
                    SendMessage sendMessage = transport.command(messageReceived, SendMessage.class);
                    sendMessage.prepare().putLong(sendMessage.offset(), newNumber);
                    sendMessage.commit(8);
                    transport.handle(sendMessage);

                    messageListener.accept(newNumber);
                }
            }

            @Override
            public void work()
            {
                transport.work();
            }

            private void connect()
            {
                transport.handle(transport.command(Connect.class).set("localhost", port, CONNECT_COMMAND_ID, 100, new FixedLengthDelineationType(8)));
            }
        };
    }
}
