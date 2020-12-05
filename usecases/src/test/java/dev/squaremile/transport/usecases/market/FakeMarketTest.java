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
        assertThat(new FakeMarket(initialPrice, (currentTime, previousPrice) -> previousPrice).midPrice()).isEqualTo(initialPrice);
    }

    @Test
    void shouldMoveThePriceEveryTick()
    {
        assertThat(new FakeMarket(100, (currentTime, previousPrice) -> previousPrice).tick(0L).midPrice()).isEqualTo(100);
        assertThat(new FakeMarket(100, (currentTime, previousPrice) -> previousPrice).tick(0L).tick(0L).midPrice()).isEqualTo(100);
        assertThat(new FakeMarket(100, (currentTime, previousPrice) -> previousPrice + 2).tick(0L).midPrice()).isEqualTo(102);
        assertThat(new FakeMarket(100, (currentTime, previousPrice) -> previousPrice + 2).tick(0L).tick(0L).midPrice()).isEqualTo(104);
        assertThat(new FakeMarket(200, (currentTime, previousPrice) -> previousPrice + -3).tick(0L).midPrice()).isEqualTo(197);
        assertThat(new FakeMarket(200, (currentTime, previousPrice) -> previousPrice + -3).tick(0L).tick(0L).midPrice()).isEqualTo(194);
        assertThat(new FakeMarket(10, (currentTime, previousPrice) -> previousPrice + (previousPrice / 2)).tick(0L).midPrice()).isEqualTo(15);
        assertThat(new FakeMarket(10, (currentTime, previousPrice) -> previousPrice + (previousPrice / 2)).tick(0L).tick(0L).midPrice()).isEqualTo(22);
    }

    @Test
    void shouldInformAboutTimeWhenCalculatingNewPrice()
    {
        assertThat(new FakeMarket(100L, (currentTime, previousPrice) -> previousPrice + currentTime / 10).tick(123).midPrice()).isEqualTo(112);
        assertThat(new FakeMarket(100L, (currentTime, previousPrice) -> previousPrice - currentTime).tick(10).tick(44).midPrice()).isEqualTo(46);
    }
}