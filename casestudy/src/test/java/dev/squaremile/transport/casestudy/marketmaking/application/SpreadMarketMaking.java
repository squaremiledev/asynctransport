package dev.squaremile.transport.casestudy.marketmaking.application;

import org.agrona.collections.MutableLong;


import dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;
import dev.squaremile.transport.casestudy.marketmaking.domain.Security;

class SpreadMarketMaking implements MarketApplication
{
    final MarketMakerPublisher marketMakerPublisher;
    final MutableLong correlationId = new MutableLong(0);
    final FirmPrice firmPricePublication = FirmPrice.createNoPrice();
    private final int spread;
    private final int warmUpUpdates;
    private long securityUpdatesCount;

    SpreadMarketMaking(final MarketMakerPublisher marketMakerPublisher, final int spread, final int warmUpUpdates)
    {
        this.marketMakerPublisher = marketMakerPublisher;
        this.spread = spread;
        this.warmUpUpdates = warmUpUpdates;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        if (!(marketMessage instanceof Security))
        {
            return;
        }
        securityUpdatesCount++;
        Security security = (Security)marketMessage;
        if (securityUpdatesCount < warmUpUpdates)
        {
            return;
        }
        marketMakerPublisher.publish(
                firmPricePublication.update(
                        correlationId.incrementAndGet(),
                        security.lastUpdateTime(),
                        security.midPrice() - spread,
                        100,
                        security.midPrice() + spread,
                        100
                ));
    }

    @Override
    public void work()
    {

    }

    public boolean hasWarmedUp()
    {
        return securityUpdatesCount > warmUpUpdates;
    }
}
