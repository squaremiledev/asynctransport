package dev.squaremile.transport.usecases.market.domain;

public class Volatility implements MidPriceUpdate
{
    private final TrendSetter trendSetter;
    private Trend currentTrend;
    private long lastTimeTrendChanged;

    public Volatility(final TrendSetter trendSetter)
    {
        this.trendSetter = trendSetter;
    }

    @Override
    public long newMidPrice(final long currentTime, final Security security)
    {
        if (currentTrend == null)
        {
            lastTimeTrendChanged = currentTime;
            currentTrend = trendSetter.trend(currentTime, 0);
            return currentTrend.newMidPrice(currentTime, security);
        }
        final Trend nextTrend = trendSetter.trend(currentTime, currentTime - lastTimeTrendChanged);
        if (!nextTrend.trendName().equals(currentTrend.trendName()))
        {
            lastTimeTrendChanged = currentTime;
        }
        currentTrend = nextTrend;
        return currentTrend.newMidPrice(currentTime, security);
    }

    public interface TrendSetter
    {
        Trend trend(long currentTime, final long timeSinceTrendChanged);
    }
}
