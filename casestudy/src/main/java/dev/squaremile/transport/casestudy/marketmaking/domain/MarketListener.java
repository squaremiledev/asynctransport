package dev.squaremile.transport.casestudy.marketmaking.domain;

public interface MarketListener extends TickListener, ExecutionReportListener, FirmPriceUpdateListener, OrderResultListener
{
    static MarketListener marketListeners(MarketListener... listeners)
    {
        final MarketListener[] marketListeners = new MarketListener[listeners.length];
        System.arraycopy(listeners, 0, marketListeners, 0, listeners.length);
        return new MarketListener()
        {
            @Override
            public void onExecution(final ExecutionReport executionReport)
            {
                for (final MarketListener marketListener : marketListeners)
                {
                    marketListener.onExecution(executionReport);
                }
            }

            @Override
            public void onFirmPriceUpdate(final int marketMakerId, final FirmPrice firmPrice)
            {
                for (final MarketListener marketListener : marketListeners)
                {
                    marketListener.onFirmPriceUpdate(marketMakerId, firmPrice);
                }
            }

            @Override
            public void onOrderResult(final int marketParticipantId, final OrderResult orderResult)
            {
                for (final MarketListener marketListener : marketListeners)
                {
                    marketListener.onOrderResult(marketParticipantId, orderResult);
                }
            }

            @Override
            public void onTick(final Security security)
            {
                for (final MarketListener marketListener : marketListeners)
                {
                    marketListener.onTick(security);
                }
            }
        };
    }

    interface MarketMessageListener
    {
        MarketMessageListener IGNORE = marketMessage ->
        {
        };

        void onMessage(MarketMessage marketMessage);
    }
}
