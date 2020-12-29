package dev.squaremile.transport.casestudy.marketmaking.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice;

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
