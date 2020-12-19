package dev.squaremile.transport.usecases.market.domain;

import java.util.concurrent.ThreadLocalRandom;

public class RandomizedTrend implements MidPriceUpdate, Trend
{
    private final String trendName;
    private final long period;
    private final int delta;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int base;

    public RandomizedTrend(final String trendName, final int base, final int delta, final long period)
    {
        this.trendName = trendName;
        this.delta = delta;
        this.base = base;
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
        int totalChange = base + random.nextInt(delta);
        return security.midPrice() + (timeSinceLastChange / period) * totalChange;
    }

    @Override
    public String trendName()
    {
        return trendName;
    }

    @Override
    public String toString()
    {
        return "Trend{" +
               "trendName='" + trendName + '\'' +
               ", period=" + period +
               ", delta=" + delta +
               '}';
    }
}
