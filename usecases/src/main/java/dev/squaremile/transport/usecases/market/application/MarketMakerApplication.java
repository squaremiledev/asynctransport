package dev.squaremile.transport.usecases.market.application;

import java.util.function.Consumer;


import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Security;

public class MarketMakerApplication implements BusinessApplication
{
    private static final int SPREAD = 100;

    private final MarketMakerPublisher marketMakerPublisher;
    private final FirmPrice lastUpdatedFirmPrice = FirmPrice.createNoPrice();
    private final FirmPrice firmPricePublication = FirmPrice.createNoPrice();
    private final ExecutionReport lastExecutedOrder = new ExecutionReport();
    private int acknowledgedPriceUpdatesCount = 0;
    private int executedReportsCount = 0;
    private int securityUpdatesCount = 0;
    private Consumer<Security> onSecurityUpdate = security ->
    {

    };

    public MarketMakerApplication(final MarketMakerPublisher marketMakerPublisher)
    {
        this.marketMakerPublisher = marketMakerPublisher;
    }

    public void configureOnSecurityUpdate(final Consumer<Security> onSecurityUpdate)
    {
        this.onSecurityUpdate = onSecurityUpdate;
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
    public void onPeriodicWakeUp()
    {

    }

    private void onSecurityUpdate(final Security security)
    {
        onSecurityUpdate.accept(security);
        securityUpdatesCount++;
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

    public MarketMakerPublisher marketMakerPublisher()
    {
        return marketMakerPublisher;
    }

    public FirmPrice firmPricePublication()
    {
        return firmPricePublication;
    }
}
