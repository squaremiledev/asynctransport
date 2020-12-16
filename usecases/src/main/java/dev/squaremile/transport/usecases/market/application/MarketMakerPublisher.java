package dev.squaremile.transport.usecases.market.application;

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
        // TODO: encode the actual message instead
        SendMessage sendMessage = connectionTransport.command(SendMessage.class);
        sendMessage.prepare().putLong(sendMessage.offset(), firmPrice.updateTime());
        sendMessage.commit(Long.BYTES);
        connectionTransport.handle(sendMessage);
    }
}
