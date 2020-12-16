package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.transport.usecases.market.domain.FirmPrice;

public class MarketMakerApplication
{
    private final MarketMakerPublisher marketMakerPublisher;
    private long inFlightCorrelationId;
    private int acknowledgedPriceUpdatesCount = 0;
    private long lastAcknowledgedCorrelationId;

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
        this.lastAcknowledgedCorrelationId = correlationId;
        this.acknowledgedPriceUpdatesCount++;
    }

    public int acknowledgedPriceUpdatesCount()
    {
        return acknowledgedPriceUpdatesCount;
    }

    public long lastAcknowledgedCorrelationId()
    {
        return lastAcknowledgedCorrelationId;
    }
}
