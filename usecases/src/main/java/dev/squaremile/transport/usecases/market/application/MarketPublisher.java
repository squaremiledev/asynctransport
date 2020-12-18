package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.OrderResult;

public class MarketPublisher
{
    private final MarketMessagePublisher publisher;

    public MarketPublisher(final ConnectionTransport connectionTransport)
    {
        publisher = new MarketMessagePublisher(connectionTransport);
    }

    public void publish(final FirmPrice message)
    {
        publisher.publish(message);
    }

    public void publish(final OrderResult message)
    {
        publisher.publish(message);
    }
}
