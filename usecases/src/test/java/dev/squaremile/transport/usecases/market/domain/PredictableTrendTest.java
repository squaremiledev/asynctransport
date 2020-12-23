package dev.squaremile.transport.usecases.market.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictableTrendTest
{
    @Test
    void shouldNotMovePriceIfTimeDidNotMoveEnough()
    {
        PredictableTrend trend = new PredictableTrend("trend", 5, 2);
        assertThat(trend.newMidPrice(0, new TrackedSecurity(0, 100, 0)).midPrice()).isEqualTo(100);
        assertThat(trend.newMidPrice(1, new TrackedSecurity(0, 100, 0)).midPrice()).isEqualTo(100);
    }

    @Test
    void shouldMovePriceAccordingToTimeSinceLastUpdateAndVolatilityFactor()
    {
        PredictableTrend trend = new PredictableTrend("trend", 5, 2);
//        assertThat(trend.newMidPrice(2, new TrackedSecurity(1, 100, 0)).midPrice()).isEqualTo(105);
        assertThat(trend.newMidPrice(3, new TrackedSecurity(1, 100, 0)).midPrice()).isEqualTo(105);
        assertThat(trend.newMidPrice(12, new TrackedSecurity(2, 105, 2)).midPrice()).isEqualTo(130);
    }
}