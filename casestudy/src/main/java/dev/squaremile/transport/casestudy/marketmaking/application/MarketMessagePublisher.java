package dev.squaremile.transport.casestudy.marketmaking.application;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;

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
