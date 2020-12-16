package dev.squaremile.transport.usecases.market.domain;

public interface ExecutionListener
{
    void onExecutedOrder(int passiveParticipantId, int aggressiveParticipantId, Order executedOrder);
}
