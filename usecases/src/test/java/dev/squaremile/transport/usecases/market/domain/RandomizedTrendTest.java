package dev.squaremile.transport.usecases.market.domain;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static java.util.stream.IntStream.generate;
import static java.util.stream.LongStream.range;

class RandomizedTrendTest
{
    private final TrackedSecurity security = new TrackedSecurity().midPrice(0, 10);
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Test
    void shouldBePredictableWhenTheRandomFactorIsEliminated()
    {
        RandomizedTrend trend = new RandomizedTrend("trendUp", 1, 0, 0);
        assertThat(range(1001, 1006)
                           .mapToObj(time -> updatePrice(trend, time))
                           .collect(Collectors.toList())
        ).isEqualTo(Arrays.asList(10, 11, 12, 13, 14));
    }

    @Test
    void shouldCalculateWhereThePriceShouldBeAfterMissingSomeUpdates()
    {
        RandomizedTrend trend = new RandomizedTrend("trendUp", 1, 0, 0);
        security.midPrice(0, 10);
        assertThat(IntStream.of(1000, 1005, 1009)
                           .mapToObj(time -> updatePrice(trend, time))
                           .collect(Collectors.toList())
        ).isEqualTo(Arrays.asList(10, 15, 19));
    }

    @Test
    void shouldSkipSomeUpdates()
    {
        RandomizedTrend trend = new RandomizedTrend("trendUp", 1, 0, 3);
        assertThat(range(1001, 1010)
                           .mapToObj(time -> updatePrice(trend, time))
                           .collect(Collectors.toList())
        ).isEqualTo(Arrays.asList(10, 10, 10, 11, 11, 11, 12, 12, 12));
    }

    @Test
    @Disabled
    void shouldCalculateWhereThePriceShouldBeAfterSkippingSomeUpdates()
    {
        RandomizedTrend trend = new RandomizedTrend("trendUp", 3, 0, 3);
        security.midPrice(0, 10);
        assertThat(IntStream.of(1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012)
                           .mapToObj(time -> updatePrice(trend, time))
                           .collect(Collectors.toList())
        ).isEqualTo(Arrays.asList(10, 10, 10, 13, 13, 13, 16, 16, 16, 19, 19, 19, 22));
        security.midPrice(0, 10);
        List<Integer> pricesRefreshedEveryTick = IntStream.range(1000, 2000).mapToObj(time -> updatePrice(trend, time)).collect(Collectors.toList());
        assertThat(pricesRefreshedEveryTick.get(pricesRefreshedEveryTick.size() -1)).isEqualTo(1009);


        security.midPrice(0, 10);
        assertThat(IntStream.of(1000, 1005, 1011)
                           .mapToObj(time -> updatePrice(trend, time))
                           .collect(Collectors.toList())
        ).isEqualTo(Arrays.asList(10, 13, 19));
        security.midPrice(0, 10);
        List<Integer> pricesRefreshedAfterLag = IntStream.of(1000, 2000).mapToObj(time -> updatePrice(trend, time)).collect(Collectors.toList());
        assertThat(pricesRefreshedAfterLag.get(pricesRefreshedAfterLag.size() -1)).isEqualTo(1009);
    }

    @Test
    void shouldUseTheRandomFactor()
    {
        final int maxRandomIncrease = 20;
        final int constantChangeFactor = -10;
        final int updatesCount = 1_000_000;
        final double allowedAverageDivergencePerPriceUpdate = 0.1;
        assertThat(generate(() -> constantChangeFactor + random.nextInt(maxRandomIncrease + 1))
                           .limit(updatesCount)
                           .average()
                           .orElseThrow(IllegalArgumentException::new)
        ).isCloseTo(0, Offset.offset(1.0));

        security.midPrice(0, 0);
        RandomizedTrend trend = new RandomizedTrend("trend", constantChangeFactor, maxRandomIncrease, 0);
        List<Integer> prices = range(1001, 1001 + updatesCount)
                .mapToObj(time -> updatePrice(trend, time))
                .collect(Collectors.toList());
        assertThat(prices).hasSize(updatesCount);
        assertThat((int)prices.get(prices.size() - 1)).isCloseTo(0, Offset.offset((int)(allowedAverageDivergencePerPriceUpdate * updatesCount)));
    }

    private int updatePrice(final RandomizedTrend trend, final long time)
    {
        return (int)trend.newMidPrice(time, security).midPrice();
    }
}