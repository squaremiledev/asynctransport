package dev.squaremile.transport.usecases.market;

import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.collections.MutableLong;

public class PnL implements MarketListener
{
    private static final MutableLong NO_BALANCE = new MutableLong(0);
    private final Int2ObjectHashMap<MutableLong> traderPnLs = new Int2ObjectHashMap<>();

    @Override
    public void onExecution(final int passiveTraderId, final int aggressiveTraderId, final Security tradedSecurity, final Order executingOrder)
    {
        long pnlFromThisOrder = 0;
        switch (executingOrder.side())
        {
            case BID:
                pnlFromThisOrder = (executingOrder.bidPrice() - tradedSecurity.midPrice()) * executingOrder.bidQuantity();
                break;
            case ASK:
                pnlFromThisOrder = (tradedSecurity.midPrice() - executingOrder.askPrice()) * executingOrder.askQuantity();
                break;
        }
        traderPnLs.computeIfAbsent(passiveTraderId, __ -> new MutableLong(0)).addAndGet(pnlFromThisOrder);
        traderPnLs.computeIfAbsent(aggressiveTraderId, __ -> new MutableLong(0)).addAndGet(-pnlFromThisOrder);
    }

    public long estimatedNominalBalanceOf(int traderId)
    {
        return traderPnLs.getOrDefault(traderId, NO_BALANCE).get();
    }
}
