package dev.squaremile.asynctcpacceptance.sampleapps;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

import static java.lang.System.nanoTime;

class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;

    public EchoConnectionApplication(final ConnectionTransport connectionTransport, final ConnectionId connectionId)
    {
        this.connectionTransport = connectionTransport;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            long sendTimeNs = messageReceived.buffer().getLong(messageReceived.offset());
            SendMessage message = connectionTransport.command(SendMessage.class);
            MutableDirectBuffer buffer = message.prepare();
            buffer.putLong(message.offset(), sendTimeNs);
            buffer.putLong(message.offset() + 8, nanoTime());
            message.commit(16);
            connectionTransport.handle(message);
        }
    }
}
