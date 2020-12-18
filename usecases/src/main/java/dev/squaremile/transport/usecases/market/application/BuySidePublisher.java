package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.transport.usecases.market.domain.Order;

public class BuySidePublisher
{
    private final MarketMessagePublisher publisher;

    public BuySidePublisher(final ConnectionTransport connectionTransport)
    {
        publisher = new MarketMessagePublisher(connectionTransport);
    }

    public void publish(final Order message)
    {
        publisher.publish(message);
    }
}
