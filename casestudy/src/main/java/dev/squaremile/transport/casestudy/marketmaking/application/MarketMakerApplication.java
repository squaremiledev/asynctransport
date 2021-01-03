package dev.squaremile.transport.casestudy.marketmaking.application;

import java.util.function.Consumer;


import dev.squaremile.transport.casestudy.marketmaking.domain.ExecutionReport;
import dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;
import dev.squaremile.transport.casestudy.marketmaking.domain.Security;

public class MarketMakerApplication implements MarketApplication
{
    private final MarketMessagePublisher marketMessagePublisher;
    private final FirmPrice lastUpdatedFirmPrice = FirmPrice.createNoPrice();
    private final ExecutionReport lastExecutedOrder = new ExecutionReport();
    private final Consumer<MarketMessage> marketMessageListener;
    private int acknowledgedPriceUpdatesCount = 0;
    private int executedReportsCount = 0;
    private int securityUpdatesCount = 0;

    public MarketMakerApplication(final MarketMessagePublisher marketMessagePublisher, final Consumer<MarketMessage> marketMessageListener)
    {
        this.marketMessagePublisher = marketMessagePublisher;
        this.marketMessageListener = marketMessageListener;
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
        if (marketMessage instanceof Security)
        {
            onSecurityUpdate((Security)marketMessage);
        }
    }

    @Override
    public void work()
    {

    }

    private void onSecurityUpdate(final Security security)
    {
        marketMessageListener.accept(security);
        securityUpdatesCount++;
    }

    public void updatePrice(final FirmPrice firmPrice)
    {
        marketMessagePublisher.publish(firmPrice);
    }

    public void onFirmPriceUpdated(final FirmPrice updatedFirmPrice)
    {
        lastUpdatedFirmPrice.update(updatedFirmPrice.updateTime(), updatedFirmPrice);
        this.acknowledgedPriceUpdatesCount++;
    }

    public void onExecutedOrder(final ExecutionReport executedOrder)
    {
        this.lastExecutedOrder.update(executedOrder);
        this.executedReportsCount++;
    }

    public int executedReportsCount()
    {
        return executedReportsCount;
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

    public int midPriceUpdatesCount()
    {
        return securityUpdatesCount;
    }
}
