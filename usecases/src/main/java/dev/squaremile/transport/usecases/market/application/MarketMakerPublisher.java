package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

public class MarketMakerPublisher
{
    private final MarketMessagePublisher publisher;

    public MarketMakerPublisher(final ConnectionTransport connectionTransport)
    {
        publisher = new MarketMessagePublisher(connectionTransport);
    }

    public void publish(final FirmPrice message)
    {
        publisher.publish(message);
    }
}