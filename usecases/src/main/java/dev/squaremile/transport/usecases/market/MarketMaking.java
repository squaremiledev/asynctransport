package dev.squaremile.transport.usecases.market;

import java.util.Map;
import java.util.function.IntFunction;

import org.agrona.collections.Int2ObjectHashMap;

public class MarketMaking
{
    private static final IntFunction<FirmPrice> NO_FIRM_PRICE = participant -> FirmPrice.createNoPrice();
    private final Int2ObjectHashMap<FirmPrice> participantFirmPrice = new Int2ObjectHashMap<>();
    private final Order executedOrderResult;
    private final ExecutionListener executionListener;

    public MarketMaking(final ExecutionListener executionListener)
    {
        this.executionListener = executionListener;
        this.executedOrderResult = new Order(0, 0, 0, 0);
    }

    public FirmPrice firmPrice(final int marketParticipantId)
    {
        return participantFirmPrice.computeIfAbsent(marketParticipantId, NO_FIRM_PRICE);
    }

    public void updateFirmPrice(final long currentTime, final int marketParticipantId, final FirmPrice marketMakerFirmPrice)
    {
        firmPrice(marketParticipantId).update(currentTime, marketMakerFirmPrice);
    }

    public boolean execute(final long currentTime, final int executingMarketParticipant, final Order order)
    {
        Int2ObjectHashMap<FirmPrice>.EntryIterator iterator = participantFirmPrice.entrySet().iterator();
        FirmPrice bestPrice = null;
        int matchedMarketMaker = -1;
        while (iterator.hasNext())
        {
            Map.Entry<Integer, FirmPrice> entry = iterator.next();
            FirmPrice price = entry.getValue();
            if (order.side() == Side.ASK)
            {
                bestPrice = bestBidPrice(order, bestPrice, price);
            }
            else if (order.side() == Side.BID)
            {
                bestPrice = bestAskPrice(order, bestPrice, price);
            }
            if (bestPrice == price)
            {
                // TODO: avoid autoboxing
                matchedMarketMaker = entry.getKey();
            }
        }
        boolean hasExecuted = bestPrice != null && bestPrice.execute(currentTime, order, executedOrderResult);
        if (hasExecuted)
        {
            executionListener.onExecutedOrder(matchedMarketMaker, executingMarketParticipant, executedOrderResult);
        }
        return hasExecuted;
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
