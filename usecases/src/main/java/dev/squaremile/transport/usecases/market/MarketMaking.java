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
            if (order.askQuantity() > 0)
            {
                if (price.bidQuantity() >= order.askQuantity() && (bestPrice == null || price.bidPrice() >= order.askPrice()))
                {
                    bestPrice = price;
                }
            }
            else if (order.bidQuantity() > 0)
            {
                if (price.askQuantity() >= order.bidQuantity() &&
                    (
                            bestPrice == null || (
                                    price.askPrice() < order.bidPrice()
                            )
                    )
                )
                {
                    bestPrice = price;
                }
            }
        }
        return bestPrice != null && bestPrice.execute(currentTime, order);
    }
}
