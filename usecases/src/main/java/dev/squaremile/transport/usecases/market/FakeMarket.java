package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final PriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();

    public FakeMarket(final long initialMidPrice, final PriceUpdate priceUpdate)
    {
        this.priceMovement = priceUpdate;
        this.security.midPrice(0L, initialMidPrice);
    }

    public long midPrice()
    {
        return security.midPrice();
    }

    public FakeMarket tick(final long currentTime)
    {
        security.midPrice(currentTime, priceMovement.newPrice(currentTime, security));
        return this;

    }

    @FunctionalInterface
    interface PriceUpdate
    {
        long newPrice(long currentTime, Security security);
    }
}
