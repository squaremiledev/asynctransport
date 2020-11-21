package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.function.LongConsumer;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

class LongPingPongAppFactory implements ApplicationFactory
{
    private final int port;
    private final EventListener pingSpy;
    private final LongConsumer messageListener;
    private final int messagesCap;
    private final Delineation delineation;

    public LongPingPongAppFactory(final Delineation delineation, final int messagesCap, final int port, final EventListener pingSpy, final LongConsumer messageListener)
    {
        this.port = port;
        this.pingSpy = pingSpy;
        this.messageListener = messageListener;
        this.messagesCap = messagesCap;
        this.delineation = delineation;
    }

    @Override
    public EventDrivenApplication create(final Transport transport)
    {
        return new EventDrivenApplication()
        {
            private int numberCount = 0;

            @Override
            public void onStart()
            {
                transport.handle(transport.command(Listen.class).set(1, port, delineation));
            }

            @Override
            public void onEvent(final Event event)
            {
                pingSpy.onEvent(event);
                if (event instanceof ConnectionAccepted)
                {
                    ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
                    SendMessage sendMessage = transport.command(connectionAccepted.connectionId(), SendMessage.class);
                    int newNumber = 1;
                    MutableDirectBuffer buffer = sendMessage.prepare();
                    buffer.putLong(sendMessage.offset(), newNumber);
                    sendMessage.commit(8);
                    transport.handle(sendMessage);

                    messageListener.accept(newNumber);
                }
                if (event instanceof MessageReceived)
                {

                    MessageReceived messageReceived = (MessageReceived)event;
                    numberCount++;
                    if (numberCount >= messagesCap)
                    {
                        transport.handle(transport.command(messageReceived.connectionId(), CloseConnection.class));
                        return;
                    }
                    long numberReceived = messageReceived.buffer().getLong(messageReceived.offset());
                    long newNumber = numberReceived + 1_000_000;
                    SendMessage sendMessage = transport.command(messageReceived.connectionId(), SendMessage.class);
                    sendMessage.prepare().putLong(sendMessage.offset(), newNumber);
                    sendMessage.commit(8);
                    transport.handle(sendMessage);

                    messageListener.accept(newNumber);
                }
            }
        };
    }
}
