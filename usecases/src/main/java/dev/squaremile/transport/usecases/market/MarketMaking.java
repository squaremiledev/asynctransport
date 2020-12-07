package dev.squaremile.transport.usecases.market;

import java.util.function.IntFunction;

import org.agrona.collections.Int2ObjectHashMap;

public class MarketMaking
{
    private static final IntFunction<FirmPrice> NO_FIRM_PRICE = participant -> FirmPrice.createNoPrice();
    private final Int2ObjectHashMap<FirmPrice> participantFirmPrice = new Int2ObjectHashMap<>();
    private int knownMarketParticipantId;

    public FirmPrice firmPrice(final int marketParticipantId)
    {
        return participantFirmPrice.computeIfAbsent(marketParticipantId, NO_FIRM_PRICE);
    }

    public void updateFirmPrice(final long currentTime, final int marketParticipantId, final FirmPrice marketMakerFirmPrice)
    {
        firmPrice(marketParticipantId).update(currentTime, marketMakerFirmPrice);
        this.knownMarketParticipantId = marketParticipantId;
    }

    public boolean execute(final long currentTime, final Order order)
    {
        Int2ObjectHashMap<FirmPrice>.EntryIterator iterator = participantFirmPrice.entrySet().iterator();
        FirmPrice bestPrice = null;
        while (iterator.hasNext())
        {
            FirmPrice price = iterator.next().getValue();
            if (order.side() == Side.ASK)
            {
                bestPrice = bestBidPrice(order, bestPrice, price);
            }
            else if (order.side() == Side.BID)
            {
                bestPrice = bestAskPrice(order, bestPrice, price);
            }
        }
        return bestPrice != null && bestPrice.execute(currentTime, order);
    }

    private FirmPrice bestAskPrice(final Order order, FirmPrice bestPriceSoFar, final FirmPrice price)
    {
        if (price.askQuantity() >= order.bidQuantity() && price.askPrice() <= order.bidPrice())
        {
            if (bestPriceSoFar == null || (price.askPrice() < bestPriceSoFar.askPrice() || (price.askPrice() == bestPriceSoFar.askPrice() && price.updateTime() < bestPriceSoFar.updateTime())))
            {
                bestPriceSoFar = price;
            }
        }
        return bestPriceSoFar;
    }

    private FirmPrice bestBidPrice(final Order order, FirmPrice bestPriceSoFar, final FirmPrice price)
    {
        if (price.bidQuantity() >= order.askQuantity() && price.bidPrice() >= order.askPrice())
        {
            if (bestPriceSoFar == null || (price.bidPrice() > bestPriceSoFar.bidPrice() || (price.bidPrice() == bestPriceSoFar.bidPrice() && price.updateTime() < bestPriceSoFar.updateTime())))
            {
                bestPriceSoFar = price;
            }
        }
        return bestPriceSoFar;
    }
}
