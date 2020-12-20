package dev.squaremile.transport.usecases.market.domain;

import java.util.ArrayList;
import java.util.List;

public class Volatility implements MidPriceUpdate
{
    private final List<Trend> trends = new ArrayList<>();
    private final long changeTrendFrequency;
    private long lastTimeTrendChanged;
    private int nextTrend = 0;

    public Volatility(final long changeTrendFrequency, final List<Trend> trends)
    {
        this.changeTrendFrequency = changeTrendFrequency;
        this.trends.addAll(trends);
    }

    @Override
    public long newMidPrice(final long currentTime, final Security security)
    {
        return trend(currentTime).newMidPrice(currentTime, security);
    }

    private Trend trend(final long currentTime)
    {
        long timeSinceTrendChanged = currentTime - lastTimeTrendChanged;
        if (timeSinceTrendChanged >= changeTrendFrequency)
        {
            nextTrend++;
            lastTimeTrendChanged = currentTime;
        }
        return trends.get(nextTrend % trends.size());
    }
}
