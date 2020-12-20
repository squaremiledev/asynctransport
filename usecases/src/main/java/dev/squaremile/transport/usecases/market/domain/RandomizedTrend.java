package dev.squaremile.transport.usecases.market.domain;

import java.util.concurrent.ThreadLocalRandom;

public class RandomizedTrend implements MidPriceUpdate, Trend
{
    private final String trendName;
    private final int maxRandomIncrease;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int baseDifference;
    private final long updateFrequency;

    public RandomizedTrend(final String trendName, final int baseDifference, final int maxRandomIncrease, final long updateFrequency)
    {
        this.trendName = trendName;
        this.maxRandomIncrease = maxRandomIncrease;
        this.baseDifference = baseDifference;
        this.updateFrequency = updateFrequency;
    }

    @Override
    public Security newMidPrice(final long currentTime, final TrackedSecurity security)
    {
        if (security.lastUpdateTime() == 0)
        {
            security.midPrice(currentTime, security.midPrice());
            return security;
        }
        long timeSinceLastUpdated = currentTime - security.lastUpdateTime();
        if (timeSinceLastUpdated < updateFrequency)
        {
            return security;
        }
        int totalChange = baseDifference + random.nextInt(maxRandomIncrease + 1);
        long delayFactor = updateFrequency < 1 ? timeSinceLastUpdated : timeSinceLastUpdated / updateFrequency;
        security.midPrice(currentTime, security.midPrice() + delayFactor * totalChange);
        return security;
    }

    @Override
    public String trendName()
    {
        return trendName;
    }

    @Override
    public String toString()
    {
        return "RandomizedTrend{" +
               "trendName='" + trendName + '\'' +
               ", maxRandomIncrease=" + maxRandomIncrease +
               ", baseDifference=" + baseDifference +
               '}';
    }
}
