package dev.squaremile.transport.usecases.market;

public class IncrementAfterNTimeUnits implements PriceUpdate
{

    private final int timeUnits;
    private final int priceIncrement;

    public IncrementAfterNTimeUnits(final int timeUnits, final int priceIncrement)
    {
        this.timeUnits = timeUnits;
        this.priceIncrement = priceIncrement;
    }

    @Override
    public long newPrice(final long currentTime, final Security security)
    {
        return security.midPrice() + (currentTime - security.lastPriceChange() >= timeUnits ? priceIncrement : 0);
    }
}
