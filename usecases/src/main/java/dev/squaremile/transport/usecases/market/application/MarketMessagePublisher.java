package dev.squaremile.transport.usecases.market.application;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;

public class MarketMessagePublisher
{
    private final ConnectionTransport connectionTransport;
    private final Serialization serialization = new Serialization();

    public MarketMessagePublisher(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
    }

    public void publish(final MarketMessage marketMessage)
    {
        SendMessage sendMessage = connectionTransport.command(SendMessage.class);
        MutableDirectBuffer buffer = sendMessage.prepare();
        int encodedLength = serialization.encode(marketMessage, buffer, sendMessage.offset());
        sendMessage.commit(encodedLength);
        connectionTransport.handle(sendMessage);
    }
}
