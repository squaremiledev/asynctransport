package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final MidPriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();
    private final TickListener tickListener;
    private final MarketMaking marketMaking;
    private final MarketListener marketListener;
    private long currentTime;

    public FakeMarket(final Security security, final MidPriceUpdate priceMovement, final TickListener tickListener, final MarketListener marketListener)
    {
        this.security.update(security);
        this.priceMovement = priceMovement;
        this.tickListener = tickListener;
        this.marketListener = marketListener;
        this.marketMaking = new MarketMaking(
                (marketMakerId, executingMarketParticipant, executedOrder) ->
                        marketListener.onExecution(marketMakerId, executingMarketParticipant, this.security, executedOrder));
    }

    public long midPrice()
    {
        return security.midPrice();
    }

    public FakeMarket tick(final long currentTime)
    {
        validateTime(currentTime);
        security.midPrice(currentTime, priceMovement.newMidPrice(currentTime, security));
        tickListener.onTick(security);
        this.currentTime = currentTime;
        return this;
    }

    public void onFirmPriceUpdate(final long currentTime, final int marketParticipant, final FirmPrice marketMakerFirmPrice)
    {
        validateTime(currentTime);
        marketMaking.updateFirmPrice(currentTime, marketParticipant, marketMakerFirmPrice);
        tick(currentTime);
    }

    public boolean execute(final long currentTime, final int executingMarketParticipant, final Order order)
    {
        if (currentTime >= this.currentTime && marketMaking.execute(currentTime, executingMarketParticipant, order))
        {
            tick(currentTime);
            return true;
        }
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
