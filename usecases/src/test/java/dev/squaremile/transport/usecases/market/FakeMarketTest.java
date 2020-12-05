package dev.squaremile.transport.usecases.market;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FakeMarketTest
{
    @ParameterizedTest
    @ValueSource(longs = {100, 0, -3})
    void shouldProvideInitialMidPrice(final long initialPrice)
    {
        assertThat(new FakeMarket(initialPrice, previousPrice -> previousPrice).midPrice()).isEqualTo(initialPrice);
    }

    @Test
    void shouldMoveThePriceEveryTick()
    {
        assertThat(new FakeMarket(100, previousPrice -> previousPrice).tick().midPrice()).isEqualTo(100);
        assertThat(new FakeMarket(100, previousPrice -> previousPrice).tick().tick().midPrice()).isEqualTo(100);
        assertThat(new FakeMarket(100, previousPrice -> previousPrice + 2).tick().midPrice()).isEqualTo(102);
        assertThat(new FakeMarket(100, previousPrice -> previousPrice + 2).tick().tick().midPrice()).isEqualTo(104);
        assertThat(new FakeMarket(200, previousPrice -> previousPrice + -3).tick().midPrice()).isEqualTo(197);
        assertThat(new FakeMarket(200, previousPrice -> previousPrice + -3).tick().tick().midPrice()).isEqualTo(194);
        assertThat(new FakeMarket(10, previousPrice -> previousPrice + (previousPrice / 2)).tick().midPrice()).isEqualTo(15);
        assertThat(new FakeMarket(10, previousPrice -> previousPrice + (previousPrice / 2)).tick().tick().midPrice()).isEqualTo(22);
    }
}