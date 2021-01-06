package dev.squaremile.transport.casestudy.marketmaking.application;

import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.transport.casestudy.marketmaking.domain.Order;

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
