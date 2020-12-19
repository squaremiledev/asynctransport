package dev.squaremile.transport.usecases.market.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictableTrendTest
{
    @Test
    void shouldNotMovePriceIfTimeDidNotMoveEnough()
    {
        PredictableTrend trend = new PredictableTrend("trend", 5, 2);
        assertThat(trend.newMidPrice(0, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(trend.newMidPrice(1, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
    }

    @Test
    void shouldMovePriceAccordingToTimeSinceLastUpdateAndVolatilityFactor()
    {
        PredictableTrend trend = new PredictableTrend("trend", 5, 2);
        assertThat(trend.newMidPrice(2, new TrackedSecurity(1, 100, 0))).isEqualTo(105);
        assertThat(trend.newMidPrice(12, new TrackedSecurity(2, 105, 2))).isEqualTo(130);
    }

    @Test
    void shouldDecideTheExactPriceChange()
    {
        PredictableTrend trend = new PredictableTrend("trend", 1, 1);
        assertThat(trend.newMidPrice(2, new TrackedSecurity(1, 100, 0))).isEqualTo(102);

        trend.moveNextPriceBy(1);

        assertThat(trend.newMidPrice(12, new TrackedSecurity(2, 102, 2))).isEqualTo(103);
        assertThat(trend.newMidPrice(13, new TrackedSecurity(12, 103, 12))).isEqualTo(104);
    }
}