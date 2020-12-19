package dev.squaremile.transport.usecases.market.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VolatilityTest
{
    @Test
    void shouldFollowTheTrend()
    {
        Volatility volatility = new Volatility((__, i) -> new PredictableTrend("trend", 1, 1));
        assertThat(volatility.newMidPrice(0, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(volatility.newMidPrice(1, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(volatility.newMidPrice(2, new TrackedSecurity(1, 100, 0))).isEqualTo(102);
        assertThat(volatility.newMidPrice(3, new TrackedSecurity(2, 102, 2))).isEqualTo(103);
    }

    @Test
    void shouldDecideOnTheNewTrend()
    {
        Volatility.TrendSetter alteringTrend = new Volatility.TrendSetter()
        {
            private PredictableTrend currentTrend = new PredictableTrend("trendUp", 10, 1);

            @Override
            public PredictableTrend trend(final long currentTime, final long timeSinceTrendChanged)
            {
                if (timeSinceTrendChanged > 10)
                {
                    currentTrend = "trendUp".equals(currentTrend.trendName()) ? new PredictableTrend("trendDown", -5, 1) : new PredictableTrend("trendUp", 10, 1);
                }
                return currentTrend;
            }
        };
        Volatility volatility = new Volatility(alteringTrend);

        assertThat(volatility.newMidPrice(0, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(volatility.newMidPrice(1, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(volatility.newMidPrice(2, new TrackedSecurity(1, 100, 0))).isEqualTo(120);
        assertThat(volatility.newMidPrice(3, new TrackedSecurity(2, 120, 2))).isEqualTo(130);
        assertThat(volatility.newMidPrice(10, new TrackedSecurity(3, 130, 3))).isEqualTo(200);
        assertThat(volatility.newMidPrice(11, new TrackedSecurity(10, 200, 10))).isEqualTo(195);
        assertThat(volatility.newMidPrice(20, new TrackedSecurity(11, 195, 11))).isEqualTo(150);
        assertThat(volatility.newMidPrice(21, new TrackedSecurity(20, 150, 20))).isEqualTo(145);
        assertThat(volatility.newMidPrice(22, new TrackedSecurity(21, 145, 21))).isEqualTo(155);
    }
}