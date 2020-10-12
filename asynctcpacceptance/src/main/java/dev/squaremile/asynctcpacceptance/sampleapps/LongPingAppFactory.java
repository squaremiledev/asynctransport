package dev.squaremile.asynctcpacceptance.sampleapps;

import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

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
    public EventDrivenApplication create(final Transport transport)
    {
        return new EventDrivenApplication()
        {
            private ConnectionIdValue connectionId;
            private int numberCount = 0;

            @Override
            public void onStart()
            {
                transport.handle(transport.command(Listen.class).set(1, port, Delineation.fixedLengthDelineation(8)));
            }

            @Override
            public void work()
            {
                if (connectionId != null)
                {
                    for (int i = 0; i < 1000; i++)
                    {
                        if (numberCount < messagesCap)
                        {
                            SendMessage sendMessage = transport.command(connectionId, SendMessage.class).commandId(numberCount);
                            sendMessage.prepare().putLong(sendMessage.offset(), numberCount);
                            sendMessage.commit(8);
                            transport.handle(sendMessage);
                            numberCount++;
                        }
                    }
                }
            }

            @Override
            public void onEvent(final Event event)
            {
                pingSpy.onEvent(event);
                if (event instanceof ConnectionAccepted)
                {
                    connectionId = new ConnectionIdValue((ConnectionAccepted)event);
                }
                else if (event instanceof ConnectionClosed || event instanceof ConnectionResetByPeer)
                {
                    connectionId = null;
                }
            }
        };
    }
}
