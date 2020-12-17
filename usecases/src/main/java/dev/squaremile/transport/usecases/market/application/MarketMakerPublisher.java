package dev.squaremile.transport.usecases.market.application;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

public class MarketMakerPublisher
{

    private final ConnectionTransport connectionTransport;

    public MarketMakerPublisher(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
    }

    public void publish(final FirmPrice firmPrice)
    {
        SendMessage sendMessage = connectionTransport.command(SendMessage.class);
        MutableDirectBuffer buffer = sendMessage.prepare();
        buffer.putLong(sendMessage.offset(), firmPrice.correlationId());
        buffer.putLong(sendMessage.offset() + Long.BYTES, firmPrice.updateTime());
        buffer.putLong(sendMessage.offset() + Long.BYTES * 2, firmPrice.bidPrice());
        buffer.putLong(sendMessage.offset() + Long.BYTES * 3, firmPrice.bidQuantity());
        buffer.putLong(sendMessage.offset() + Long.BYTES * 3 + Integer.BYTES, firmPrice.askPrice());
        buffer.putLong(sendMessage.offset() + Long.BYTES * 4 + Integer.BYTES, firmPrice.askQuantity());
        sendMessage.commit(Long.BYTES * 4 + Integer.BYTES * 2);
        connectionTransport.handle(sendMessage);
    }
}
