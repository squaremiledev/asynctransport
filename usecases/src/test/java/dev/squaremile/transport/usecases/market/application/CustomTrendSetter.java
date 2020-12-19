package dev.squaremile.transport.usecases.market.application;

import java.util.ArrayList;
import java.util.List;


import dev.squaremile.transport.usecases.market.domain.Trend;
import dev.squaremile.transport.usecases.market.domain.Volatility;

public class CustomTrendSetter implements Volatility.TrendSetter
{
    private final List<Trend> trends = new ArrayList<>();
    private int nextTrend = 0;
    private Trend currentTrend;
    private final long changeTrendFrequency;

    public CustomTrendSetter(final long changeTrendFrequency, final List<Trend> trends)
    {
        this.trends.addAll(trends);
        currentTrend = trends.get(0);
        this.changeTrendFrequency = changeTrendFrequency;
    }

    @Override
    public Trend trend(final long currentTime, final long timeSinceTrendChanged)
    {
        if (timeSinceTrendChanged > changeTrendFrequency)
        {
            nextTrend++;
            currentTrend = trends.get(nextTrend % trends.size());
        }
        return currentTrend;
    }
}
