package dev.squaremile.transport.usecases.market.domain;

public interface MarketListener extends TickListener, ExecutionReportListener, FirmPriceUpdateListener, OrderResultListener
{
}
