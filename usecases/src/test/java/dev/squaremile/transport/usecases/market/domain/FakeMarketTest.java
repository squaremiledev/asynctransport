package dev.squaremile.transport.usecases.market.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static dev.squaremile.transport.usecases.market.domain.FirmPrice.spreadFirmPrice;
import static java.util.stream.IntStream.range;

class FakeMarketTest
{

    private static final int MARKET_MAKER = 555;
    private static final int ARBITRAGEUR = 444;

    @ParameterizedTest
    @ValueSource(longs = {100, 0, -3})
    void shouldProvideInitialMidPrice(final long initialPrice)
    {
        assertThat(fakeMarket(initialPrice, (currentTime, security) -> security.midPrice(), new PnL()).midPrice()).isEqualTo(initialPrice);
    }

    @Test
    void shouldMoveThePriceEveryTick()
    {
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice(), new PnL()).tick(0L).midPrice()).isEqualTo(100);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice(), new PnL()).tick(0L).tick(0L).midPrice()).isEqualTo(100);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice() + 2, new PnL()).tick(0L).midPrice()).isEqualTo(102);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice() + 2, new PnL()).tick(0L).tick(0L).midPrice()).isEqualTo(104);
        assertThat(fakeMarket(200, (currentTime, security) -> security.midPrice() + -3, new PnL()).tick(0L).midPrice()).isEqualTo(197);
        assertThat(fakeMarket(200, (currentTime, security) -> security.midPrice() + -3, new PnL()).tick(0L).tick(0L).midPrice()).isEqualTo(194);
        assertThat(fakeMarket(10, (currentTime, security) -> security.midPrice() + (security.midPrice() / 2), new PnL()).tick(0L).midPrice()).isEqualTo(15);
        assertThat(fakeMarket(10, (currentTime, security) -> security.midPrice() + (security.midPrice() / 2), new PnL()).tick(0L).tick(0L).midPrice()).isEqualTo(22);
    }

    @Test
    void shouldInformAboutTimeWhenCalculatingNewPrice()
    {
        assertThat(fakeMarket(100L, (currentTime, security) -> security.midPrice() + currentTime / 10, new PnL()).tick(123).midPrice()).isEqualTo(112);
        assertThat(fakeMarket(100L, (currentTime, security) -> security.midPrice() - currentTime, new PnL()).tick(10).tick(44).midPrice()).isEqualTo(46);
    }

    @Test
    void shouldInformAboutLastUpdateTimeWhenCalculatingNewPrice()
    {
        assertThat(fakeMarket(100, (currentTime, security) -> security.lastUpdateTime(), new PnL()).midPrice()).isEqualTo(100);
        assertThat(fakeMarket(100, (currentTime, security) -> security.lastUpdateTime(), new PnL()).tick(5).midPrice()).isEqualTo(0);
        assertThat(fakeMarket(100, (currentTime, security) -> security.lastUpdateTime(), new PnL()).tick(5).tick(10).midPrice()).isEqualTo(5);
        assertThat(fakeMarket(100, (currentTime, security) -> security.midPrice() + security.lastUpdateTime(), new PnL()).tick(10).tick(11).midPrice()).isEqualTo(110);
    }

    @Test
    void shouldAllowSteadyPriceUpdates()
    {
        FakeMarket fakeMarket = fakeMarket(100, new Volatility(3, 10), new PnL());
        range(1, 201).forEach(fakeMarket::tick);
        assertThat(fakeMarket.midPrice()).isEqualTo(160);
    }

    @Test
    void shouldInformAboutTickingSecurity()
    {
        final TickerSpy tickerSpy = new TickerSpy();
        FakeMarket fakeMarket = new FakeMarket(
                new TrackedSecurity().midPrice(0, 100),
                new Volatility(3, 2),
                tickerSpy,
                new PnL()
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
        FakeMarket fakeMarket = fakeMarket(20, new Volatility(1, 10), new PnL());
        assertThat(fakeMarket.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(FirmPrice.createNoPrice());
    }

    @Test
    void shouldShowMostRecentFirmPrice()
    {
        FakeMarket fakeMarket = fakeMarket(20, new Volatility(1, 10), new PnL());
        FirmPrice firmPrice1 = new FirmPrice(0, 19, 60, 21, 50);
        fakeMarket.onFirmPriceUpdate(1001, MARKET_MAKER, firmPrice1);

        // When
        FirmPrice firmPrice2 = new FirmPrice(0, 20, 80, 22, 70);
        fakeMarket.onFirmPriceUpdate(1002, MARKET_MAKER, firmPrice2);
        firmPrice2.update(1003, FirmPrice.createNoPrice()); // to verify that the unit under test does not rely on the mutable reference to the price

        // Then
        assertThat(fakeMarket.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1002, 20, 80, 22, 70));
    }

    @Test
    void shouldExecuteFirmPriceAndUpdateItWhenBidding()
    {
        PnL pnL = new PnL();
        FakeMarket fakeMarket = fakeMarket(20, (currentTime, security) -> security.midPrice(), pnL);
        fakeMarket.onFirmPriceUpdate(1001, MARKET_MAKER, new FirmPrice(0, 19, 60, 22, 50));

        assertThat(fakeMarket.execute(1002, ARBITRAGEUR, Order.bid(22, 10))).isTrue();

        // Then
        assertThat(fakeMarket.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1002, 19, 60, 22, 40));
        assertThat(pnL.estimatedNominalBalanceOf(MARKET_MAKER)).isEqualTo(20);
        assertThat(pnL.estimatedNominalBalanceOf(ARBITRAGEUR)).isEqualTo(-20);
    }

    @Test
    void shouldExecuteFirmPriceAndUpdateItWhenAsking()
    {
        PnL pnL = new PnL();
        FakeMarket fakeMarket = fakeMarket(20, (currentTime, security) -> security.midPrice(), pnL);
        fakeMarket.onFirmPriceUpdate(1001, MARKET_MAKER, new FirmPrice(0, 18, 60, 21, 50));

        assertThat(fakeMarket.execute(1002, ARBITRAGEUR, Order.ask(18, 10))).isTrue();

        // Then
        assertThat(fakeMarket.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1002, 18, 50, 21, 50));
        assertThat(pnL.estimatedNominalBalanceOf(MARKET_MAKER)).isEqualTo(20);
        assertThat(pnL.estimatedNominalBalanceOf(ARBITRAGEUR)).isEqualTo(-20);
    }

    @Test
    void shouldKeepTheOriginalFirmPriceWhenFailedToExecute()
    {
        FakeMarket fakeMarket = fakeMarket(20, new Volatility(1, 10), new PnL());
        fakeMarket.onFirmPriceUpdate(1001, MARKET_MAKER, new FirmPrice(0, 19, 60, 21, 50));

        assertThat(fakeMarket.execute(1002, ARBITRAGEUR, Order.bid(21, 51))).isFalse();

        // Then
        assertThat(fakeMarket.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1001, 19, 60, 21, 50));
    }

    @Test
    void shouldNotAcceptTimeMovingBack()
    {
        List<Long> securityUpdateTimes = new ArrayList<>();
        FakeMarket fakeMarket = new FakeMarket(
                new TrackedSecurity().midPrice(0, 1_000_000),
                (currentTime, security) -> security.midPrice() + 1,
                security -> securityUpdateTimes.add(security.lastUpdateTime()),
                new PnL()
        );
        fakeMarket.onFirmPriceUpdate(1, MARKET_MAKER, new FirmPrice(0, 19, 60, 23, 50));
        fakeMarket.tick(2);

        // When
        assertThrows(IllegalArgumentException.class, () -> fakeMarket.tick(1));
        assertThrows(IllegalArgumentException.class, () -> fakeMarket.onFirmPriceUpdate(1, MARKET_MAKER, new FirmPrice(0, 19, 60, 22, 50)));
        assertThat(fakeMarket.execute(1, ARBITRAGEUR, Order.ask(19, 10))).isFalse();

        // Then
        assertThat(securityUpdateTimes).isEqualTo(Arrays.asList(1L, 2L));
        assertThat(fakeMarket.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 60, 23, 50));
    }

    @Test
    void shouldSimulateMarketMakerLosingMoneyDueToFirmPriceUpdateDelayInVolatileMarketConditions()
    {
        final int spreadInPips = 10;
        final int lotSize = 1_000_000;
        final int nominalFirmPriceSize = 5 * lotSize;
        final int nominalOrderSize = 2 * lotSize;
        final PnL pnL = new PnL();
        final TimeMachine timeMachine = new TimeMachine(0);
        final FakeMarket market = fakeMarket(14500, new Volatility(3, 10), pnL); // ~ a pip every 10/3 milliseconds (Brexit referendum results anyone?)
        timeMachine.tick(
                // Not enough liquidity for ARBITRAGEUR to execute any orders
                time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, nominalOrderSize))).isFalse(),
                // MARKET_MAKER sends their firm price with a non skewed spread around the market mid price
                time -> market.onFirmPriceUpdate(time, MARKET_MAKER, spreadFirmPrice(time, nominalFirmPriceSize, market.midPrice(), spreadInPips)),
                market::tick,
                // MARKET_MAKER keeps updating their firm price with a non skewed spread around the market mid price
                time -> market.onFirmPriceUpdate(time, MARKET_MAKER, spreadFirmPrice(timeMachine.time(), nominalFirmPriceSize, market.midPrice(), spreadInPips)),
                // MARKET_MAKER spread protected against ARBITRAGEUR - no order executed
                time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, nominalOrderSize))).isFalse()
        );
        timeMachine.tick(44, market::tick);
        timeMachine.tick(time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, nominalOrderSize))).isFalse());
        // MARKET_MAKER still spread protected against ARBITRAGEUR - no order executed
        assertThat(market.midPrice()).isEqualTo(14512L);
        assertThat(market.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(3, 14490, nominalFirmPriceSize, 14510, nominalFirmPriceSize));
        timeMachine.tick(time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, nominalOrderSize))).isFalse());

        // MARKET_MAKER has not updated the price for a while as it experiences a GC pause / OS jitter / algo issues - their spread can no longer offset the price movement
        assertThat(market.midPrice()).isEqualTo(14515L);
        int expectedUpdateTimeBeforeOrderMatched = 3;
        assertThat(market.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(expectedUpdateTimeBeforeOrderMatched, 14490, nominalFirmPriceSize, 14510, nominalFirmPriceSize));
        assertThat(pnL.estimatedNominalBalanceOf(MARKET_MAKER)).isEqualTo(0);
        assertThat(pnL.estimatedNominalBalanceOf(ARBITRAGEUR)).isEqualTo(0);
        timeMachine.tick(time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, 2 * lotSize))).isTrue());
        timeMachine.tick(time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, 2 * lotSize))).isTrue());
        timeMachine.tick(time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, 2 * lotSize))).isFalse());
        long expectedNominalAmountTraded = 20000000L;
        assertThat(pnL.estimatedNominalBalanceOf(MARKET_MAKER)).isEqualTo(-expectedNominalAmountTraded);
        assertThat(pnL.estimatedNominalBalanceOf(ARBITRAGEUR)).isEqualTo(expectedNominalAmountTraded);
        assertThat(market.midPrice()).isEqualTo(14515L);
        int expectedUpdateTimeAfterOrderMatched = 52;
        assertThat(market.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(
                expectedUpdateTimeAfterOrderMatched,
                14490,
                nominalFirmPriceSize,
                14510,
                nominalFirmPriceSize - 4 * lotSize
        ));

        // In such volatile conditions, for the spread of 10 pips, 49 ms delay in price updates is enough to lose 20M
        assertThat(expectedNominalAmountTraded).isEqualTo(20_000_000);
        assertThat(expectedUpdateTimeAfterOrderMatched - expectedUpdateTimeBeforeOrderMatched).isEqualTo(49);
    }

    private FakeMarket fakeMarket(final long initialPrice, final MidPriceUpdate priceMovement, final MarketListener marketListener)
    {
        return new FakeMarket(new TrackedSecurity().midPrice(0, initialPrice), priceMovement, security ->
        {
        }, marketListener);
    }
}