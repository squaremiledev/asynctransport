package dev.squaremile.transport.usecases.market;

public interface ExecutionListener
{
    void onExecutedOrder(int passiveParticipantId, int aggressiveParticipantId, Order executedOrder);
}
