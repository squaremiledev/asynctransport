package dev.squaremile.transport.usecases.market.domain;

public class FakeMarket
{
    private final MidPriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();
    private final TickListener tickListener;
    private final MarketMaking marketMaking;
    private final ExecutionReport executionReport = new ExecutionReport();
    private final FirmPriceUpdateListener firmPriceUpdateListener;
    private final OrderResultListener orderResultListener;
    private final FirmPrice firmPriceUpdate = FirmPrice.createNoPrice();
    private final long tickCoolDownTime;
    private long currentTime;

    public FakeMarket(
            final Security security,
            final MidPriceUpdate priceMovement,
            final long tickCoolDownTime,
            final MarketListener marketListener
    )
    {
        this(security, priceMovement, tickCoolDownTime, marketListener, marketListener, marketListener, marketListener);
    }

    public FakeMarket(
            final Security security,
            final MidPriceUpdate priceMovement,
            final long tickCoolDownTime,
            final TickListener tickListener,
            final ExecutionReportListener executionReportListener,
            final FirmPriceUpdateListener firmPriceUpdateListener,
            final OrderResultListener orderResultListener
    )
    {
        this.security.update(security);
        this.priceMovement = priceMovement;
        this.tickListener = tickListener;
        this.firmPriceUpdateListener = firmPriceUpdateListener;
        this.orderResultListener = orderResultListener;
        this.marketMaking = new MarketMaking(
                (passiveParticipantId, aggressiveParticipantId, executedOrder) ->
                        executionReportListener.onExecution(executionReport.update(passiveParticipantId, aggressiveParticipantId, this.security, executedOrder)));
        this.tickCoolDownTime = tickCoolDownTime;
    }

    public long midPrice()
    {
        return security.midPrice();
    }

    public FakeMarket tick(final long currentTime)
    {
        if (this.currentTime + tickCoolDownTime > currentTime)
        {
            return this;
        }
        validateTime(currentTime);
        priceMovement.newMidPrice(currentTime, security);
        tickListener.onTick(security);
        this.currentTime = currentTime;
        return this;
    }

    public void onFirmPriceUpdate(final long currentTime, final int marketParticipant, final FirmPrice marketMakerFirmPrice)
    {
        tick(currentTime);
        marketMaking.updateFirmPrice(currentTime, marketParticipant, marketMakerFirmPrice);
        firmPriceUpdate.update(currentTime, marketMaking.firmPrice(marketParticipant));
        firmPriceUpdateListener.onFirmPriceUpdate(marketParticipant, firmPriceUpdate);
    }

    public boolean execute(final long currentTime, final int executingMarketParticipant, final Order order)
    {
        if (currentTime < this.currentTime)
        {
            orderResultListener.onOrderResult(executingMarketParticipant, OrderResult.NOT_EXECUTED);
            return false;
        }
        if (marketMaking.execute(currentTime, executingMarketParticipant, order))
        {
            orderResultListener.onOrderResult(executingMarketParticipant, OrderResult.EXECUTED);
            tick(currentTime);
            return true;
        }
        orderResultListener.onOrderResult(executingMarketParticipant, OrderResult.NOT_EXECUTED);
        tick(currentTime);
        return false;
    }

    private void validateTime(final long currentTime)
    {
        if (this.currentTime > currentTime)
        {
            throw new IllegalArgumentException("Provided time " + currentTime + " is behind the current market time " + this.currentTime);
        }
    }

    public FirmPrice firmPrice(final int marketParticipant)
    {
        return marketMaking.firmPrice(marketParticipant);
    }
}
