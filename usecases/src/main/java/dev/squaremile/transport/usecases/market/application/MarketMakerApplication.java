package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.transport.usecases.market.domain.FirmPrice;

public class MarketMakerApplication
{
    private final MarketMakerPublisher marketMakerPublisher;
    private final FirmPrice lastUpdatedFirmPrice = FirmPrice.createNoPrice();
    private int acknowledgedPriceUpdatesCount = 0;

    public MarketMakerApplication(final MarketMakerPublisher marketMakerPublisher)
    {
        this.marketMakerPublisher = marketMakerPublisher;
    }

    public void updatePrice(final FirmPrice firmPrice)
    {
        marketMakerPublisher.publish(firmPrice);
    }

    public void onFirmPriceUpdated(final FirmPrice updatedFirmPrice)
    {
        lastUpdatedFirmPrice.update(updatedFirmPrice.updateTime(), updatedFirmPrice);
        this.acknowledgedPriceUpdatesCount++;
    }

    public int acknowledgedPriceUpdatesCount()
    {
        return acknowledgedPriceUpdatesCount;
    }

    public FirmPrice lastUpdatedFirmPrice()
    {
        return lastUpdatedFirmPrice;
    }
}
