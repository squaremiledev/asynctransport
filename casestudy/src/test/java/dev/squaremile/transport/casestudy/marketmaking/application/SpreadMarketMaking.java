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
    private long receivedMessagesCount;

    SpreadMarketMaking(final MarketMessagePublisher marketMessagePublisher, final int spread)
    {
        this.marketMessagePublisher = marketMessagePublisher;
        this.spread = spread;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        receivedMessagesCount++;
        if (marketMessage instanceof HeartBeat)
        {
            marketMessagePublisher.publish(HeartBeat.INSTANCE);
        }
        else if (marketMessage instanceof Security)
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
    }

    public long receivedMessagesCount()
    {
        return receivedMessagesCount;
    }

    @Override
    public void work()
    {

    }
}
