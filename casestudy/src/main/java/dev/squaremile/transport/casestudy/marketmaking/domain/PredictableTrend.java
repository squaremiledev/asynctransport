package dev.squaremile.transport.casestudy.marketmaking.domain;

public class PredictableTrend implements Trend
{
    private final RandomizedTrend trend;

    public PredictableTrend(final String trendName, final int delta, final long period)
    {
        this.trend = new RandomizedTrend(trendName, delta, 0, period);
    }

    @Override
    public Security newMidPrice(final long currentTime, final TrackedSecurity security)
    {
        return trend.newMidPrice(currentTime, security);
    }

    @Override
    public String trendName()
    {
        return trend.trendName();
    }

    @Override
    public String toString()
    {
        return trend.toString();
    }
}
