package dev.squaremile.transport.usecases.market.domain;

public class ExecutionReport implements MarketMessage
{
    private final TrackedSecurity security = new TrackedSecurity();
    private final Order executedOrder = new Order(0, 0, 0, 0);
    private int passiveTraderId;
    private int aggressiveTraderId;

    public ExecutionReport update(final ExecutionReport source)
    {
        return update(
                source.passiveTraderId,
                source.aggressiveTraderId,
                source.security(),
                source.executedOrder()
        );
    }

    public ExecutionReport update(final int passiveTraderId, final int aggressiveTraderId, final Security security, final Order executedOrder)
    {
        this.passiveTraderId = passiveTraderId;
        this.aggressiveTraderId = aggressiveTraderId;
        this.security.update(security);
        this.executedOrder.update(executedOrder);
        return this;
    }

    public Security security()
    {
        return security;
    }

    public int passiveTraderId()
    {
        return passiveTraderId;
    }

    public int aggressiveTraderId()
    {
        return aggressiveTraderId;
    }

    public Order executedOrder()
    {
        return executedOrder;
    }

    @Override
    public String toString()
    {
        return "ExecutionReport{" +
               "passiveTraderId=" + passiveTraderId +
               ", aggressiveTraderId=" + aggressiveTraderId +
               ", security=" + security +
               ", executedOrder=" + executedOrder +
               '}';
    }
}
