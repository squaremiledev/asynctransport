package dev.squaremile.transport.usecases.market;

public class FakeMarket
{
    private final PriceUpdate priceMovement;

    private long currentMidPrice;

    public FakeMarket(final long initialMidPrice, final PriceUpdate priceUpdate)
    {
        this.currentMidPrice = initialMidPrice;
        this.priceMovement = priceUpdate;
    }

    public long midPrice()
    {
        return currentMidPrice;
    }

    public FakeMarket tick(final long currentTime)
    {
        currentMidPrice = priceMovement.newPrice(currentTime, currentMidPrice);
        return this;

    }

    @FunctionalInterface
    interface PriceUpdate
    {
        long newPrice(final long currentTime, long oldPrice);
    }
}
