package dev.squaremile.transport.usecases.market.domain;

public class PredictableTrend implements Trend
{
    private final String trendName;
    private final long period;
    private final int delta;
    private int moveNextPriceBy;

    public PredictableTrend(final String trendName, final int delta, final long period)
    {
        this.trendName = trendName;
        this.delta = delta;
        this.period = period;
    }

    @Override
    public long newMidPrice(final long currentTime, final Security security)
    {
        if (moveNextPriceBy != 0)
        {
            long newPrice = security.midPrice() + moveNextPriceBy;
            moveNextPriceBy = 0;
            return newPrice;
        }

        long timeSinceLastChange = currentTime - security.lastPriceChange();
        if (security.lastUpdateTime() == 0 || timeSinceLastChange <= 0)
        {
            return security.midPrice();
        }
        return security.midPrice() + (timeSinceLastChange / period) * delta;
    }

    public void moveNextPriceBy(final int value)
    {
        moveNextPriceBy = value;
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
               ", moveNextPriceBy=" + moveNextPriceBy +
               '}';
    }
}
