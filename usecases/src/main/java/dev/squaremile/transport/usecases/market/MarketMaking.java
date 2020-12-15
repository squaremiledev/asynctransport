package dev.squaremile.transport.usecases.market;

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

    public void updateFirmPrice(final long currentTime, final int marketParticipantId, final FirmPrice marketMakerFirmPrice)
    {
        firmPrice(marketParticipantId).update(currentTime, marketMakerFirmPrice);
    }

    public boolean execute(final long currentTime, final int executingMarketParticipant, final Order aggressiveOrder)
    {
        FirmPrice bestPrice = null;
        int matchedMarketMakerId = -1;

        final Int2ObjectHashMap<FirmPrice>.EntryIterator iterator = marketMakersFirmPrice.entrySet().iterator();
        while (iterator.hasNext())
        {
            iterator.next();
            final int marketMakerId = iterator.getIntKey();
            final FirmPrice price = iterator.getValue();
            if (aggressiveOrder.side() == Side.ASK)
            {
                bestPrice = bestPassiveBidPrice(aggressiveOrder, bestPrice, price);
            }
            else if (aggressiveOrder.side() == Side.BID)
            {
                bestPrice = bestPassiveAskPrice(aggressiveOrder, bestPrice, price);
            }
            if (bestPrice == price)
            {
                matchedMarketMakerId = marketMakerId;
            }
        }
        boolean hasExecuted = bestPrice != null && bestPrice.execute(currentTime, aggressiveOrder, executedOrderResult);
        if (hasExecuted)
        {
            executionListener.onExecutedOrder(matchedMarketMakerId, executingMarketParticipant, executedOrderResult);
        }
        return hasExecuted;
    }

    private FirmPrice bestPassiveAskPrice(final Order order, FirmPrice bestPriceSoFar, final FirmPrice price)
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

    private FirmPrice bestPassiveBidPrice(final Order order, FirmPrice bestPriceSoFar, final FirmPrice price)
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
