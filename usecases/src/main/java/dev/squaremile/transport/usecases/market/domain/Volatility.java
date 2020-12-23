package dev.squaremile.transport.usecases.market.domain;

import java.util.ArrayList;
import java.util.List;

public class Volatility implements MidPriceUpdate
{
    private final List<Trend> trends = new ArrayList<>();
    private final long changeTrendFrequency;
    private long lastTimeTrendChanged;
    private int nextTrend = -1;

    public Volatility(final long changeTrendFrequency, final List<Trend> trends)
    {
        this.changeTrendFrequency = changeTrendFrequency;
        this.trends.addAll(trends);
    }

    @Override
    public Security newMidPrice(final long currentTime, final TrackedSecurity security)
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
