package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final MidPriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();
    private final TickListener tickListener;
    private final FirmPrice currentMarketMakerFirmPrice = FirmPrice.createNoPrice();

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
        security.midPrice(currentTime, priceMovement.newMidPrice(currentTime, security));
        tickListener.onTick(security);
        return this;

    }

    public FirmPrice firmPrice()
    {
        return currentMarketMakerFirmPrice;
    }

    public void onFirmPriceUpdate(final long currentTime, final FirmPrice marketMakerFirmPrice)
    {
        this.currentMarketMakerFirmPrice.update(marketMakerFirmPrice);
        tick(currentTime);
    }

    public boolean execute(final int currentTime, final FirmPrice executedQuantity)
    {
        boolean result = this.currentMarketMakerFirmPrice.execute(executedQuantity);
        tick(currentTime);
        return result;
    }
}
