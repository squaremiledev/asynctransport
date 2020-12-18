package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Order;
import dev.squaremile.transport.usecases.market.domain.OrderResult;

public class BuySideApplication implements BusinessApplication
{
    private final BuySidePublisher publisher;
    private int orderResultCount = 0;
    private OrderResult lastOrderResult;

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
        this.lastOrderResult = orderResult;
        orderResultCount++;
    }

    public OrderResult lastOrderResult()
    {
        return lastOrderResult;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        if (marketMessage instanceof OrderResult)
        {
            onOrderResult((OrderResult)marketMessage);
        }
    }

    @Override
    public void onPeriodicWakeUp()
    {

    }
}
