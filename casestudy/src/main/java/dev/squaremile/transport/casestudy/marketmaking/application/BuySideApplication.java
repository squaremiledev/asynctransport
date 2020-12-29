package dev.squaremile.transport.casestudy.marketmaking.application;

import dev.squaremile.transport.casestudy.marketmaking.domain.ExecutionReport;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;
import dev.squaremile.transport.casestudy.marketmaking.domain.Order;
import dev.squaremile.transport.casestudy.marketmaking.domain.OrderResult;
import dev.squaremile.transport.casestudy.marketmaking.domain.Security;

public class BuySideApplication implements MarketApplication
{
    private final BuySidePublisher publisher;
    private final ExecutionReport lastExecutedOrder = new ExecutionReport();
    private int orderResultCount = 0;
    private OrderResult lastOrderResult;
    private int executedReportsCount = 0;
    private int securityUpdatesCount = 0;

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

    public void onExecutedOrder(final ExecutionReport executedOrder)
    {
        this.lastExecutedOrder.update(executedOrder);
        this.executedReportsCount++;
    }

    public void onSecurityUpdate(final Security security)
    {
        securityUpdatesCount++;
    }

    public OrderResult lastOrderResult()
    {
        return lastOrderResult;
    }

    public ExecutionReport lastExecutedOrder()
    {
        return lastExecutedOrder;
    }

    public int executedReportsCount()
    {
        return executedReportsCount;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        if (marketMessage instanceof OrderResult)
        {
            onOrderResult((OrderResult)marketMessage);
        }
        if (marketMessage instanceof ExecutionReport)
        {
            onExecutedOrder((ExecutionReport)marketMessage);
        }
        if (marketMessage instanceof Security)
        {
            onSecurityUpdate((Security)marketMessage);
        }
    }

    @Override
    public void work()
    {

    }

    public int midPriceUpdatesCount()
    {
        return securityUpdatesCount;
    }
}
