package dev.squaremile.transport.casestudy.marketmaking.application;

import org.agrona.collections.MutableLong;


import dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice;
import dev.squaremile.transport.casestudy.marketmaking.domain.HeartBeat;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;
import dev.squaremile.transport.casestudy.marketmaking.domain.Security;

class SpreadMarketMaking implements MarketApplication
{
    private final MutableLong correlationId = new MutableLong(0);
    private final FirmPrice firmPricePublication = FirmPrice.createNoPrice();
    private final MarketMessagePublisher marketMessagePublisher;
    private final int spread;
    private final int warmUpUpdates;
    private long securityUpdatesCount;

    SpreadMarketMaking(final MarketMessagePublisher marketMessagePublisher, final int spread, final int warmUpUpdates)
    {
        this.marketMessagePublisher = marketMessagePublisher;
        this.spread = spread;
        this.warmUpUpdates = warmUpUpdates;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        if (marketMessage instanceof HeartBeat)
        {
            marketMessagePublisher.publish(HeartBeat.INSTANCE);
        }
        else if (marketMessage instanceof Security)
        {
            securityUpdatesCount++;
            if (securityUpdatesCount >= warmUpUpdates)
            {
                Security security = (Security)marketMessage;
                marketMessagePublisher.publish(
                        firmPricePublication.update(
                                correlationId.incrementAndGet(),
                                security.lastUpdateTime(),
                                security.midPrice() - spread,
                                100,
                                security.midPrice() + spread,
                                100
                        ));
            }
            else
            {
                marketMessagePublisher.publish(HeartBeat.INSTANCE);
            }
        }
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
