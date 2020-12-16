package dev.squaremile.transport.usecases.market;

public class Volatility implements MidPriceUpdate
{
    private final int period;
    private final int delta;

    public Volatility(final int delta, final int period)
    {
        this.period = period;
        this.delta = delta;
    }

    @Override
    public long newMidPrice(final long currentTime, final Security security)
    {
        long timeSinceLastChange = currentTime - security.lastPriceChange();
        if (timeSinceLastChange <= 0)
        {
            return security.midPrice();
        }
        return security.midPrice() + (timeSinceLastChange / period) * delta;
    }
}
