package dev.squaremile.transport.casestudy.marketmaking.domain;

import java.util.ArrayList;
import java.util.List;

public class Volatility implements MidPriceUpdate
{
    private static final Trend NO_CHANGE = new NoChange();

    private final List<Trend> trends = new ArrayList<>();
    private final long changeTrendFrequency;
    private final long noUpdatesTimeSpan;
    private long lastTimeTrendChanged;
    private int nextTrend = -1;
    private long firstSeenTime = Long.MIN_VALUE;

    public Volatility(final long changeTrendFrequency, final long noUpdatesTimeSpan, final List<Trend> trends)
    {
        this.changeTrendFrequency = changeTrendFrequency;
        this.trends.addAll(trends);
        this.noUpdatesTimeSpan = noUpdatesTimeSpan;
    }

    @Override
    public Security newMidPrice(final long currentTime, final TrackedSecurity security)
    {
        return trend(currentTime).newMidPrice(currentTime, security);
    }

    private Trend trend(final long currentTime)
    {
        if (firstSeenTime == Long.MIN_VALUE)
        {
            this.firstSeenTime = currentTime;
        }
        if (firstSeenTime + noUpdatesTimeSpan > currentTime)
        {
            return NO_CHANGE;
        }
        long timeSinceTrendChanged = currentTime - lastTimeTrendChanged;
        if (timeSinceTrendChanged >= changeTrendFrequency)
        {
            nextTrend++;
            lastTimeTrendChanged = currentTime;
        }
        return trends.get(nextTrend % trends.size());
    }

    private static class NoChange implements Trend
    {

        @Override
        public String trendName()
        {
            return "noChange";
        }

        @Override
        public Security newMidPrice(final long currentTime, final TrackedSecurity security)
        {
            return security.midPrice(currentTime, security.midPrice());
        }
    }
}
