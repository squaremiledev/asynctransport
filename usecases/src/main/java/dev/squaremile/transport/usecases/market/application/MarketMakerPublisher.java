package dev.squaremile.transport.usecases.market.application;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

public class MarketMakerPublisher
{

    private final ConnectionTransport connectionTransport;
    private final Serialization serialization = new Serialization();

    public MarketMakerPublisher(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
    }

    public void publish(final FirmPrice firmPrice)
    {
        SendMessage sendMessage = connectionTransport.command(SendMessage.class);
        MutableDirectBuffer buffer = sendMessage.prepare();
        int offset = sendMessage.offset();
        int encodedLength = serialization.encode(firmPrice, buffer, offset);
        sendMessage.commit(encodedLength);
        connectionTransport.handle(sendMessage);
    }
}
