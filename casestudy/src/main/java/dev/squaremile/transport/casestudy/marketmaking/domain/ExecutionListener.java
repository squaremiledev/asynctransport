package dev.squaremile.transport.casestudy.marketmaking.domain;

public interface ExecutionListener
{
    void onExecutedOrder(int passiveParticipantId, int aggressiveParticipantId, Order executedOrder);
}
