package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final PriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();
    private final TickListener tickListener;

    public FakeMarket(final Security security, final PriceUpdate priceMovement, final TickListener tickListener)
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
        security.midPrice(currentTime, priceMovement.newPrice(currentTime, security));
        tickListener.onTick(security);
        return this;

    }

    @FunctionalInterface
    interface PriceUpdate
    {
        long newPrice(long currentTime, Security security);
    }

    @FunctionalInterface
    interface TickListener
    {
        void onTick(Security security);
    }
}
