package dev.squaremile.transport.usecases.market;

public interface ExecutionListener
{
    void onExecutedOrder(int marketMakerId, int executingMarketParticipant, Order executedOrder);
}
