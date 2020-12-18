package dev.squaremile.transport.usecases.market.domain;

public class Volatility implements MidPriceUpdate
{
    private final int period;
    private final int delta;

    public Volatility(final int delta, final int period)
    {
        this.delta = delta;
        this.period = period;
    }

    @Override
    public long newMidPrice(final long currentTime, final Security security)
    {
        long timeSinceLastChange = currentTime - security.lastPriceChange();
        if (security.lastUpdateTime() == 0 || timeSinceLastChange <= 0)
        {
            return security.midPrice();
        }
        return security.midPrice() + (timeSinceLastChange / period) * delta;
    }
}
