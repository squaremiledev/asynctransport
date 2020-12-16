package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

class MarketTransportApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;

    public MarketTransportApplication(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
    }

    @Override
    public void onEvent(final ConnectionEvent connectionEvent)
    {
        if (connectionEvent instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)connectionEvent;
            SendMessage sendMessage = connectionTransport.command(SendMessage.class);
            sendMessage.prepare().putLong(sendMessage.offset(), messageReceived.buffer().getLong(messageReceived.offset()));
            sendMessage.commit(Long.BYTES);
            connectionTransport.handle(sendMessage);
        }
    }
}
