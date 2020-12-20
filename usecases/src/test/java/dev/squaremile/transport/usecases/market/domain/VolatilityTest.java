package dev.squaremile.transport.usecases.market.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static java.util.Collections.singletonList;

class VolatilityTest
{
    @Test
    void shouldFollowTheTrend()
    {
        Volatility volatility = new Volatility(1000, singletonList(new PredictableTrend("trend", 1, 1)));
        assertThat(volatility.newMidPrice(0, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(volatility.newMidPrice(1, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(volatility.newMidPrice(2, new TrackedSecurity(1, 100, 0))).isEqualTo(102);
        assertThat(volatility.newMidPrice(3, new TrackedSecurity(2, 102, 2))).isEqualTo(103);
    }

    @Test
    void shouldNotChangeThePriceAtFirstUseOrIfTheTimeDidNotMove()
    {
        assertThat(new Volatility(500, singletonList(new PredictableTrend("trendUp", 10, 1))).newMidPrice(1001, new TrackedSecurity(0, 100, 0))).isEqualTo(100);
        assertThat(new Volatility(500, singletonList(new PredictableTrend("trendUp", 10, 1))).newMidPrice(1001, new TrackedSecurity(1001, 100, 1001))).isEqualTo(100);
    }

    @Test
    void shouldDecideOnTheNewTrend()
    {
        Volatility volatility = new Volatility(4, Arrays.asList(
                new PredictableTrend("trendUp", 1, 1),
                new PredictableTrend("trendDown", -1, 1)
        ));
        final TrackedSecurity security = new TrackedSecurity().midPrice(0, 10);
        List<Integer> midPrices = LongStream.range(1001, 1021).mapToObj(time -> (int)security.midPrice(time, volatility.newMidPrice(time, security)).midPrice()).collect(Collectors.toList());
        assertThat(midPrices).isEqualTo(Arrays.asList(
                10, 9, 8, 7,
                8, 9, 10, 11,
                10, 9, 8, 7,
                8, 9, 10, 11,
                10, 9, 8, 7
        ));
    }
}