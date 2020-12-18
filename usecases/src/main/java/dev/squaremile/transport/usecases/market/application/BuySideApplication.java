package dev.squaremile.transport.usecases.market.application;

public class BuySideApplication
{

    private int orderResponsesCount = 0;

    public BuySideApplication(final BuySidePublisher publisher)
    {

    }

    public int orderResponsesCount()
    {
        return orderResponsesCount;
    }

    public void sendOrder()
    {
        orderResponsesCount++;
    }
}
