package dev.squaremile.transport.usecases.market.domain;

public class FakeMarket
{
    private final MidPriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();
    private final TickListener tickListener;
    private final MarketMaking marketMaking;
    private long currentTime;

    public FakeMarket(final Security security, final MidPriceUpdate priceMovement, final TickListener tickListener, final MarketListener marketListener)
    {
        this.security.update(security);
        this.priceMovement = priceMovement;
        this.tickListener = tickListener;
        this.marketMaking = new MarketMaking(
                (passiveParticipantId, aggressiveParticipantId, executedOrder) ->
                        marketListener.onExecution(passiveParticipantId, aggressiveParticipantId, this.security, executedOrder));
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
        if (currentTime < this.currentTime)
        {
            return false;
        }
        if (marketMaking.execute(currentTime, executingMarketParticipant, order))
        {
            tick(currentTime);
            return true;
        }
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