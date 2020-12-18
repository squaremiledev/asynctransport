package dev.squaremile.transport.usecases.market.domain;

import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.collections.MutableLong;

public class PnL implements MarketListener
{
    private static final MutableLong NO_BALANCE = new MutableLong(0);
    private final Int2ObjectHashMap<MutableLong> traderPnLs = new Int2ObjectHashMap<>();

    @Override
    public void onExecution(final ExecutionReport executionReport)
    {
        long pnlFromThisOrder = 0;
        final Order executedOrder = executionReport.executedOrder();
        final Security tradedSecurity = executionReport.security();
        int passiveTraderId = executionReport.passiveTraderId();
        int aggressiveTraderId = executionReport.aggressiveTraderId();
        switch (executedOrder.side())
        {
            case BID:
                pnlFromThisOrder = (executedOrder.bidPrice() - tradedSecurity.midPrice()) * executedOrder.bidQuantity();
                break;
            case ASK:
                pnlFromThisOrder = (tradedSecurity.midPrice() - executedOrder.askPrice()) * executedOrder.askQuantity();
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
