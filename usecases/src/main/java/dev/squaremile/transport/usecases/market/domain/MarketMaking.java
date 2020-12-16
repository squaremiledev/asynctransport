package dev.squaremile.transport.usecases.market.domain;

import java.util.function.IntFunction;

import org.agrona.collections.Int2ObjectHashMap;

public class MarketMaking
{
    private static final IntFunction<FirmPrice> NO_FIRM_PRICE = participant -> FirmPrice.createNoPrice();
    private final Int2ObjectHashMap<FirmPrice> marketMakersFirmPrice = new Int2ObjectHashMap<>();
    private final Order executedOrderResult;
    private final ExecutionListener executionListener;

    public MarketMaking(final ExecutionListener executionListener)
    {
        this.executionListener = executionListener;
        this.executedOrderResult = new Order(0, 0, 0, 0);
    }

    public FirmPrice firmPrice(final int marketParticipantId)
    {
        return marketMakersFirmPrice.computeIfAbsent(marketParticipantId, NO_FIRM_PRICE);
    }

    public void updateFirmPrice(final long currentTime, final int marketMakerId, final FirmPrice firmPrice)
    {
        firmPrice(marketMakerId).update(currentTime, firmPrice);
    }

    public boolean execute(final long currentTime, final int aggressorId, final Order aggressiveOrder)
    {
        FirmPrice bestFirmPrice = null;
        int matchedMarketMakerId = -1;

        final Int2ObjectHashMap<FirmPrice>.EntryIterator marketMakers = marketMakersFirmPrice.entrySet().iterator();
        while (marketMakers.hasNext())
        {
            marketMakers.next();
            final int marketMakerId = marketMakers.getIntKey();
            final FirmPrice marketMakerFirmPrice = marketMakers.getValue();
            if (aggressiveOrder.side() == Side.ASK)
            {
                bestFirmPrice = bestPassiveBidPrice(aggressiveOrder, bestFirmPrice, marketMakerFirmPrice);
            }
            else if (aggressiveOrder.side() == Side.BID)
            {
                bestFirmPrice = bestPassiveAskPrice(aggressiveOrder, bestFirmPrice, marketMakerFirmPrice);
            }
            if (bestFirmPrice == marketMakerFirmPrice)
            {
                matchedMarketMakerId = marketMakerId;
            }
        }
        boolean hasExecuted = bestFirmPrice != null && bestFirmPrice.execute(currentTime, aggressiveOrder, executedOrderResult);
        if (hasExecuted)
        {
            executionListener.onExecutedOrder(matchedMarketMakerId, aggressorId, executedOrderResult);
        }
        return hasExecuted;
    }

    private FirmPrice bestPassiveAskPrice(final Order order, FirmPrice bestPriceSoFar, final FirmPrice priceCandidate)
    {
        if (priceCandidate.askQuantity() >= order.bidQuantity() && priceCandidate.askPrice() <= order.bidPrice())
        {
            if (bestPriceSoFar == null ||
                (priceCandidate.askPrice() < bestPriceSoFar.askPrice() || (priceCandidate.askPrice() == bestPriceSoFar.askPrice() && priceCandidate.updateTime() < bestPriceSoFar.updateTime())))
            {
                bestPriceSoFar = priceCandidate;
            }
        }
        return bestPriceSoFar;
    }

    private FirmPrice bestPassiveBidPrice(final Order order, FirmPrice bestPriceSoFar, final FirmPrice priceCandidate)
    {
        if (priceCandidate.bidQuantity() >= order.askQuantity() && priceCandidate.bidPrice() >= order.askPrice())
        {
            if (bestPriceSoFar == null ||
                (priceCandidate.bidPrice() > bestPriceSoFar.bidPrice() || (priceCandidate.bidPrice() == bestPriceSoFar.bidPrice() && priceCandidate.updateTime() < bestPriceSoFar.updateTime())))
            {
                bestPriceSoFar = priceCandidate;
            }
        }
        return bestPriceSoFar;
    }
}
