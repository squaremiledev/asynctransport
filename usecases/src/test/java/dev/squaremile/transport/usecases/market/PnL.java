package dev.squaremile.transport.usecases.market;

import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.collections.MutableLong;

public class PnL implements MarketListener
{
    private static final MutableLong NO_ENTRIES = new MutableLong(0);
    private final Int2ObjectHashMap<MutableLong> pnlPerMarketParticipant = new Int2ObjectHashMap<>();

    @Override
    public void onExecution(final int marketMakerId, final int executingMarketParticipant, final Security security, final Order executedOrder)
    {
        long pnlFromThisOrder = 0;
        switch (executedOrder.side())
        {
            case BID:
                pnlFromThisOrder = (executedOrder.bidPrice() - security.midPrice()) * executedOrder.bidQuantity();
                break;
            case ASK:
                pnlFromThisOrder = (security.midPrice() - executedOrder.askPrice()) * executedOrder.askQuantity();
                break;
        }
        pnlPerMarketParticipant.computeIfAbsent(marketMakerId, __ -> new MutableLong(0)).addAndGet(pnlFromThisOrder);
        pnlPerMarketParticipant.computeIfAbsent(executingMarketParticipant, __ -> new MutableLong(0)).addAndGet(-pnlFromThisOrder);
    }

    public long estimatedBalanceOf(int marketParticipant)
    {
        return pnlPerMarketParticipant.getOrDefault(marketParticipant, NO_ENTRIES).get();
    }
}
