package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.function.LongConsumer;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.commands.CloseConnection;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

class LongPingPongAppFactory implements TransportApplicationOnDutyFactory
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
    public TransportApplicationOnDuty create(final Transport transport)
    {
        return new TransportApplicationOnDuty()
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
