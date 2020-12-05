package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final PriceUpdate priceMovement;
    private final TrackedSecurity security = new TrackedSecurity();

    public FakeMarket(final Security security, final PriceUpdate priceMovement)
    {
        this.security.update(security);
        this.priceMovement = priceMovement;
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
