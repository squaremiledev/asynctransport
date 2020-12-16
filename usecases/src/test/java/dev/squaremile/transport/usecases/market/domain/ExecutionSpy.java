package dev.squaremile.transport.usecases.market.domain;

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
    public void onExecutedOrder(final int passiveParticipantId, final int aggressiveParticipantId, final Order executedOrder)
    {
        observedTicks.add(new Execution(passiveParticipantId, aggressiveParticipantId, new Order(executedOrder)));
    }

    static class Execution
    {
        public final int passiveParticipantId;
        public final int aggressiveParticipantId;
        public final Order order;

        public Execution(final int passiveParticipantId, final int aggressiveParticipantId, final Order order)
        {
            this.passiveParticipantId = passiveParticipantId;
            this.aggressiveParticipantId = aggressiveParticipantId;
            this.order = order;
        }

        @Override
        public String toString()
        {
            return "Execution{" +
                   "marketMakerId=" + passiveParticipantId +
                   ", order=" + order +
                   '}';
        }
    }
}
