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
        assertThat(fakeMarket(initialPrice, (currentTime, security) -> security.midPrice()).midPrice()).isEqualTo(initialPrice);
    }

    @Test
    void shouldMoveThePriceEveryTick()
    {
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice()).tick(0L).midPrice()).isEqualTo(100);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice()).tick(0L).tick(0L).midPrice()).isEqualTo(100);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice() + 2).tick(0L).midPrice()).isEqualTo(102);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice() + 2).tick(0L).tick(0L).midPrice()).isEqualTo(104);
        assertThat(fakeMarket(200, (currentTime, security) -> security.midPrice() + -3).tick(0L).midPrice()).isEqualTo(197);
        assertThat(fakeMarket(200, (currentTime, security) -> security.midPrice() + -3).tick(0L).tick(0L).midPrice()).isEqualTo(194);
        assertThat(fakeMarket(10, (currentTime, security) -> security.midPrice() + (security.midPrice() / 2)).tick(0L).midPrice()).isEqualTo(15);
        assertThat(fakeMarket(10, (currentTime, security) -> security.midPrice() + (security.midPrice() / 2)).tick(0L).tick(0L).midPrice()).isEqualTo(22);
    }

    @Test
    void shouldInformAboutTimeWhenCalculatingNewPrice()
    {
        assertThat(fakeMarket(100L, (currentTime, security) -> security.midPrice() + currentTime / 10).tick(123).midPrice()).isEqualTo(112);
        assertThat(fakeMarket(100L, (currentTime, security) -> security.midPrice() - currentTime).tick(10).tick(44).midPrice()).isEqualTo(46);
    }

    @Test
    void shouldInformAboutLastUpdateTimeWhenCalculatingNewPrice()
    {
        assertThat(fakeMarket(100, (currentTime, security) -> security.lastUpdateTime()).midPrice()).isEqualTo(100);
        assertThat(fakeMarket(100, (currentTime, security) -> security.lastUpdateTime()).tick(5).midPrice()).isEqualTo(0);
        assertThat(fakeMarket(100, (currentTime, security) -> security.lastUpdateTime()).tick(5).tick(10).midPrice()).isEqualTo(5);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice() + security.lastUpdateTime()).tick(10).tick(11).midPrice()).isEqualTo(110);
    }

    private FakeMarket fakeMarket(final long initialPrice, final FakeMarket.PriceUpdate priceMovement)
    {
        return new FakeMarket(new TrackedSecurity().midPrice(0, initialPrice), priceMovement);
    }
}