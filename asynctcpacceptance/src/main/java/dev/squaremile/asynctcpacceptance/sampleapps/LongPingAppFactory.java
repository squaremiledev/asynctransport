package dev.squaremile.asynctcpacceptance.sampleapps;

import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.FIXED_LENGTH;

class LongPingAppFactory implements ApplicationFactory
{
    private final int port;
    private final EventListener pingSpy;
    private int messagesCap;

    public LongPingAppFactory(final int messagesCap, final int port, final EventListener pingSpy)
    {
        this.port = port;
        this.pingSpy = pingSpy;
        this.messagesCap = messagesCap;
    }

    @Override
    public Application create(final Transport transport)
    {
        return new Application()
        {
            private ConnectionIdValue connectionId;
            private int numberCount = 0;

            @Override
            public void onStart()
            {
                transport.handle(transport.command(Listen.class).set(1, port, new Delineation(FIXED_LENGTH, 8)));
            }

            @Override
            public void work()
            {
                if (connectionId != null)
                {
                    if (numberCount >= messagesCap)
                    {
                        transport.handle(transport.command(connectionId, CloseConnection.class));
                        return;
                    }
                    for (int i = 0; i < 1000; i++)
                    {
                        SendMessage sendMessage = transport.command(connectionId, SendMessage.class);
                        sendMessage.prepare().putLong(sendMessage.offset(), numberCount);
                        sendMessage.commit(8);
                        transport.handle(sendMessage);
                        numberCount++;
                    }
                }
                transport.work();
            }

            @Override
            public void onEvent(final Event event)
            {
                pingSpy.onEvent(event);
                if (event instanceof ConnectionAccepted)
                {
                    connectionId = new ConnectionIdValue((ConnectionAccepted)event);
                }
            }
        };
    }
}
