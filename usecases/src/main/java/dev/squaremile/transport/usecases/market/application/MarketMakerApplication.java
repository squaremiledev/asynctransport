package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;

public class MarketMakerApplication implements BusinessApplication
{
    private final MarketMakerPublisher marketMakerPublisher;
    private final FirmPrice lastUpdatedFirmPrice = FirmPrice.createNoPrice();
    private final ExecutionReport lastExecutedOrder = new ExecutionReport();
    private int acknowledgedPriceUpdatesCount = 0;

    public MarketMakerApplication(final MarketMakerPublisher marketMakerPublisher)
    {
        this.marketMakerPublisher = marketMakerPublisher;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        if (marketMessage instanceof FirmPrice)
        {
            onFirmPriceUpdated((FirmPrice)marketMessage);
        }
        if (marketMessage instanceof ExecutionReport)
        {
            onExecutedOrder((ExecutionReport)marketMessage);
        }
    }

    @Override
    public void onPeriodicWakeUp()
    {

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

    public void onExecutedOrder(final ExecutionReport executedOrder)
    {
        this.lastExecutedOrder.update(executedOrder);
    }

    public int acknowledgedPriceUpdatesCount()
    {
        return acknowledgedPriceUpdatesCount;
    }

    public FirmPrice lastUpdatedFirmPrice()
    {
        return lastUpdatedFirmPrice;
    }

    public ExecutionReport lastExecutedOrder()
    {
        return lastExecutedOrder;
    }
}
