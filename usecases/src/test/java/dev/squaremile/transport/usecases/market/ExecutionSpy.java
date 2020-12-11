package dev.squaremile.transport.usecases.market;

import java.util.ArrayList;
import java.util.List;

public class ExecutionSpy implements ExecutionListener
{
    final List<Execution> observedTicks = new ArrayList<>();

    public static Execution execution(final int marketMakerId, final int executingMarketParticipant, final Order order)
    {
        return new Execution(marketMakerId, executingMarketParticipant, order);
    }

    public List<Execution> observedExecutions()
    {
        return observedTicks;
    }

    @Override
    public void onExecutedOrder(final int marketMakerId, final int executingMarketParticipant, final Order executedOrder)
    {
        observedTicks.add(new Execution(marketMakerId, executingMarketParticipant, new Order(executedOrder)));
    }

    static class Execution
    {
        public final int marketMakerId;
        public final Order order;

        public Execution(final int marketMakerId, final int executingMarketParticipant, final Order order)
        {
            this.marketMakerId = marketMakerId;
            this.order = order;
        }

        @Override
        public String toString()
        {
            return "Execution{" +
                   "marketMakerId=" + marketMakerId +
                   ", order=" + order +
                   '}';
        }
    }
}
