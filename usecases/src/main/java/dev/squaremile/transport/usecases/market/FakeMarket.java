package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final MidPriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();
    private final TickListener tickListener;
    private final FirmPrice currentMarketMakerFirmPrice = FirmPrice.createNoPrice();
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

    public FirmPrice firmPrice()
    {
        return currentMarketMakerFirmPrice;
    }

    public void onFirmPriceUpdate(final long currentTime, final FirmPrice marketMakerFirmPrice)
    {
        validateTime(currentTime);
        this.currentMarketMakerFirmPrice.update(marketMakerFirmPrice);
        tick(currentTime);
    }

    public boolean execute(final int currentTime, final FirmPrice executedQuantity)
    {
        if (currentTime >= this.currentTime && this.currentMarketMakerFirmPrice.execute(executedQuantity))
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
}
