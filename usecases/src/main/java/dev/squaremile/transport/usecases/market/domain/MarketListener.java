package dev.squaremile.transport.usecases.market.domain;

public interface MarketListener
{
    void onExecution(final ExecutionReport executionReport);
}
