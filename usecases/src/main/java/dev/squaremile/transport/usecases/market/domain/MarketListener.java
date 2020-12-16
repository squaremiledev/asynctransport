package dev.squaremile.transport.usecases.market.domain;

public interface MarketListener
{
    void onExecution(final int marketMakerId, final int executingMarketParticipant, final Security estimatedMidPrice, final Order executedOrder);
}
