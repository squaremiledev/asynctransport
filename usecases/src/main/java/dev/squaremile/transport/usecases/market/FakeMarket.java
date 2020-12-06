package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final MidPriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();
    private final TickListener tickListener;
    private final MarketMaking marketMaking = new MarketMaking();
    private long currentTime;

    public FakeMarket(final Security security, final MidPriceUpdate priceMovement, final TickListener tickListener)
    {
        this.security.update(security);
        this.priceMovement = priceMovement;
        this.tickListener = tickListener;
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

    public boolean execute(final long currentTime, final int executingMarketParticipant, final FirmPrice executedQuantity)
    {
        if (currentTime >= this.currentTime && marketMaking.execute(currentTime, executedQuantity))
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
