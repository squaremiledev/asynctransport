package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.transport.usecases.market.domain.FirmPrice;

public class MarketMakerApplication
{
    private final MarketMakerPublisher marketMakerPublisher;
    private long inFlightCorrelationId;
    private int acknowledgedPriceUpdatesCount = 0;

    public MarketMakerApplication(final MarketMakerPublisher marketMakerPublisher)
    {
        this.marketMakerPublisher = marketMakerPublisher;
    }

    public void updatePrice(final FirmPrice firmPrice)
    {
        marketMakerPublisher.publish(firmPrice);
        inFlightCorrelationId = firmPrice.correlationId();
    }

    public void onFirmPriceUpdated(final long correlationId)
    {
        if (correlationId == inFlightCorrelationId)
        {
            acknowledgedPriceUpdatesCount++;
        }
    }

    public int acknowledgedPriceUpdatesCount()
    {
        return acknowledgedPriceUpdatesCount;
    }
}
