package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.function.LongConsumer;


import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

class LongPongAppFactory implements TransportApplicationOnDutyFactory
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
    public TransportApplicationOnDuty create(final Transport transport)
    {
        return new TransportApplicationOnDuty()
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
                    sendMessage.prepare().putLong(sendMessage.offset(), newNumber);
                    sendMessage.commit(8);
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
