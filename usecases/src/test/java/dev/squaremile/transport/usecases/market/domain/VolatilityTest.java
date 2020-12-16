package dev.squaremile.transport.usecases.market.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VolatilityTest
{
    @Test
    void shouldNotMovePriceIfTimeDidNotMoveEnough()
    {
        Volatility volatility = new Volatility(5, 2);
        assertThat(volatility.newMidPrice(0, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(volatility.newMidPrice(1, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
    }

    @Test
    void shouldMovePriceAccordingToTimeSinceLastUpdateAndVolatilityFactor()
    {
        Volatility volatility = new Volatility(5, 2);
        assertThat(volatility.newMidPrice(2, new TrackedSecurity(1, 100, 0))).isEqualTo(105);
        assertThat(volatility.newMidPrice(12, new TrackedSecurity(2, 105, 2))).isEqualTo(130);
    }
}