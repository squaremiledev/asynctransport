package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.transport.usecases.market.domain.Order;
import dev.squaremile.transport.usecases.market.domain.OrderResult;

public class BuySideApplication
{
    private final BuySidePublisher publisher;
    private int orderResultCount = 0;

    public BuySideApplication(final BuySidePublisher publisher)
    {
        this.publisher = publisher;
    }

    public int orderResultCount()
    {
        return orderResultCount;
    }

    public void sendOrder(final Order order)
    {
        publisher.publish(order);
    }

    public void onOrderResult(final OrderResult orderResult)
    {
        orderResultCount++;
    }
}
