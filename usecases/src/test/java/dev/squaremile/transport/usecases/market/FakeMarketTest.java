package dev.squaremile.transport.usecases.market;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;


import static java.util.stream.IntStream.range;

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

    @Test
    void shouldAllowSteadyPriceUpdates()
    {
        FakeMarket fakeMarket = fakeMarket(100, new PeriodicMidPriceChange(10, 3));
        range(1, 201).forEach(fakeMarket::tick);
        assertThat(fakeMarket.midPrice()).isEqualTo(160);
    }

    @Test
    void shouldInformAboutTickingSecurity()
    {
        final TickerSpy tickerSpy = new TickerSpy();
        FakeMarket fakeMarket = new FakeMarket(
                new TrackedSecurity().midPrice(0, 100),
                new PeriodicMidPriceChange(2, 3),
                tickerSpy
        );
        range(1, 11).forEach(fakeMarket::tick);

        assertThat(tickerSpy.observedTicks()).hasSize(10);
        assertThat(tickerSpy.observedTick(0)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1, 100, 0));
        assertThat(tickerSpy.observedTick(1)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(2, 103, 2));
        assertThat(tickerSpy.observedTick(2)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(3, 103, 2));
        assertThat(tickerSpy.observedTick(3)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(4, 106, 4));
        assertThat(tickerSpy.observedTick(9)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(10, 115, 10));
    }

    @Test
    void shouldShowNoFirmPricesIfNoMarketMakerUpdates()
    {
        FakeMarket fakeMarket = fakeMarket(20, new PeriodicMidPriceChange(10, 1));
        assertThat(fakeMarket.firmPrice()).usingRecursiveComparison().isEqualTo(FirmPrice.createNoPrice());
    }

    @Test
    void shouldShowMostRecentFirmPrice()
    {
        FakeMarket fakeMarket = fakeMarket(20, new PeriodicMidPriceChange(10, 1));
        FirmPrice firmPrice1 = new FirmPrice(21, 50, 19, 60);
        fakeMarket.onFirmPriceUpdate(1001, firmPrice1);

        // When
        FirmPrice firmPrice2 = new FirmPrice(22, 70, 20, 80);
        fakeMarket.onFirmPriceUpdate(1002, firmPrice2);
        firmPrice2.update(FirmPrice.createNoPrice()); // to verify that the unit under test does not rely on the mutable reference to the price

        // Then
        assertThat(fakeMarket.firmPrice()).usingRecursiveComparison().isEqualTo(new FirmPrice(22, 70, 20, 80));
    }

    @Test
    void shouldExecuteFirmPriceAndUpdateIt()
    {
        FakeMarket fakeMarket = fakeMarket(20, new PeriodicMidPriceChange(10, 1));
        fakeMarket.onFirmPriceUpdate(1001, new FirmPrice(21, 50, 19, 60));

        assertThat(fakeMarket.execute(1002, new FirmPrice(21, 10, 19, 0))).isTrue();

        // Then
        assertThat(fakeMarket.firmPrice()).usingRecursiveComparison().isEqualTo(new FirmPrice(21, 40, 19, 60));
    }

    @Test
    void shouldKeepTheOriginalFirmPriceWhenFailedToExecute()
    {
        FakeMarket fakeMarket = fakeMarket(20, new PeriodicMidPriceChange(10, 1));
        fakeMarket.onFirmPriceUpdate(1001, new FirmPrice(21, 50, 19, 60));

        assertThat(fakeMarket.execute(1002, new FirmPrice(21, 51, 19, 0))).isFalse();

        // Then
        assertThat(fakeMarket.firmPrice()).usingRecursiveComparison().isEqualTo(new FirmPrice(21, 50, 19, 60));
    }

    private FakeMarket fakeMarket(final long initialPrice, final MidPriceUpdate priceMovement)
    {
        return new FakeMarket(new TrackedSecurity().midPrice(0, initialPrice), priceMovement, security ->
        {
        });
    }
}