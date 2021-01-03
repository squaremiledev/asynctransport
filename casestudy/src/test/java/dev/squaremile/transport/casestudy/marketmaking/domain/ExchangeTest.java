package dev.squaremile.transport.casestudy.marketmaking.domain;

import java.util.concurrent.TimeUnit;

import org.agrona.collections.MutableInteger;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;


import static dev.squaremile.transport.casestudy.marketmaking.domain.ExecutionReportListener.IGNORE;
import static dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice.spreadFirmPrice;
import static java.util.Collections.singletonList;
import static java.util.stream.IntStream.range;

class ExchangeTest
{

    private static final int MARKET_MAKER = 555;
    private static final int ARBITRAGEUR = 444;

    @ParameterizedTest
    @ValueSource(longs = {100, 0, -3})
    void shouldProvideInitialMidPrice(final long initialPrice)
    {
        assertThat(exchange(initialPrice, (currentTime, security) -> security.midPrice(currentTime, security.midPrice()), new PnL()).midPrice()).isEqualTo(initialPrice);
    }

    @Test
    void shouldMoveThePriceEveryTick()
    {
        assertThat(exchange(100, (currentTime, security) -> security.midPrice(currentTime, security.midPrice()), new PnL()).tick(1L).midPrice()).isEqualTo(100);
        assertThat(exchange(100, (currentTime, security) -> security.midPrice(currentTime, security.midPrice()), new PnL()).tick(1L).tick(2L).midPrice()).isEqualTo(100);
        assertThat(exchange(100, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + 2), new PnL()).tick(1L).midPrice()).isEqualTo(102);
        assertThat(exchange(100, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + 2), new PnL()).tick(1L).tick(2L).midPrice()).isEqualTo(104);
        assertThat(exchange(200, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + -3), new PnL()).tick(1L).midPrice()).isEqualTo(197);
        assertThat(exchange(200, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + -3), new PnL()).tick(1L).tick(2L).midPrice()).isEqualTo(194);
        assertThat(exchange(10, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + (security.midPrice() / 2)), new PnL()).tick(1L).midPrice()).isEqualTo(15);
        assertThat(exchange(10, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + (security.midPrice() / 2)), new PnL()).tick(1L).tick(2L).midPrice()).isEqualTo(22);
    }

    @Test
    void shouldInformAboutTimeWhenCalculatingNewPrice()
    {
        assertThat(exchange(100L, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + currentTime / 10), new PnL()).tick(123).midPrice()).isEqualTo(112);
        assertThat(exchange(100L, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() - currentTime), new PnL()).tick(10).tick(44).midPrice()).isEqualTo(46);
    }

    @Test
    void shouldInformAboutLastUpdateTimeWhenCalculatingNewPrice()
    {
        assertThat(exchange(100, (currentTime, security) -> security.midPrice(currentTime, security.midPrice() + security.lastUpdateTime()), IGNORE).tick(10).tick(11).midPrice()).isEqualTo(110);
    }

    @Test
    void shouldAllowSteadyPriceUpdates()
    {
        Exchange exchange = exchange(100, new PredictableTrend("trend", 3, 10), new PnL());
        range(1, 200).forEach(exchange::tick);
        assertThat(exchange.midPrice()).isEqualTo(157);
    }

    @Test
    void shouldInformAboutTickingSecurity()
    {
        final TickerSpy tickerSpy = new TickerSpy();
        MutableInteger heartBeatCount = new MutableInteger(0);
        Exchange exchange = new Exchange(
                new TrackedSecurity().midPrice(0, 100),
                new PredictableTrend("trend", 3, 2),
                4, 0,
                new MarketListenerBuilder().with((HeartBeatListener)__ -> heartBeatCount.increment()).with(tickerSpy)
        );
        range(1_000_000 - 4, 1_000_010).forEach(exchange::tick);

        assertThat(heartBeatCount.get()).isEqualTo(4);
        assertThat(tickerSpy.observedTicks()).hasSize(10);
        assertThat(tickerSpy.observedTick(0)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_000, 100, 1_000_000));
        assertThat(tickerSpy.observedTick(1)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_000, 100, 1_000_000));
        assertThat(tickerSpy.observedTick(2)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_002, 103, 1_000_002));
        assertThat(tickerSpy.observedTick(3)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_002, 103, 1_000_002));
        assertThat(tickerSpy.observedTick(4)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_004, 106, 1_000_004));
        assertThat(tickerSpy.observedTick(5)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_004, 106, 1_000_004));
        assertThat(tickerSpy.observedTick(9)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_008, 112, 1_000_008));
    }

    @Test
    void shouldCoolDownAfterEachTickNotToSendTooManyExcessiveUpdates()
    {
        final TickerSpy tickerSpy = new TickerSpy();
        final MidPriceUpdate currentTimeAsPrice = (currentTime, security) -> security.midPrice(currentTime, currentTime);
        Exchange exchange = new Exchange(
                new TrackedSecurity().midPrice(0, 100),
                currentTimeAsPrice,
                0L, 5,
                new MarketListenerBuilder().with(tickerSpy)
        );
        range(1_000_000, 1_000_016).forEach(exchange::tick);

        assertThat(tickerSpy.observedTicks()).hasSize(4);
        assertThat(tickerSpy.observedTick(0)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_000, 1_000_000, 1_000_000));
        assertThat(tickerSpy.observedTick(1)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_005, 1_000_005, 1_000_005));
        assertThat(tickerSpy.observedTick(2)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_010, 1_000_010, 1_000_010));
        assertThat(tickerSpy.observedTick(3)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(1_000_015, 1_000_015, 1_000_015));
    }

    @Test
    void shouldShowNoFirmPricesIfNoMarketMakerUpdates()
    {
        Exchange exchange = exchange(20, new PredictableTrend("trend", 1, 10), new PnL());
        assertThat(exchange.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(FirmPrice.createNoPrice());
    }

    @Test
    void shouldShowMostRecentFirmPrice()
    {
        Exchange exchange = exchange(20, new PredictableTrend("trend", 1, 10), new PnL());
        FirmPrice firmPrice1 = new FirmPrice(0, 19, 60, 21, 50);
        exchange.onFirmPriceUpdate(1001, MARKET_MAKER, firmPrice1);

        // When
        FirmPrice firmPrice2 = new FirmPrice(0, 20, 80, 22, 70);
        exchange.onFirmPriceUpdate(1002, MARKET_MAKER, firmPrice2);
        firmPrice2.update(1003, FirmPrice.createNoPrice()); // to verify that the unit under test does not rely on the mutable reference to the price

        // Then
        assertThat(exchange.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1002, 20, 80, 22, 70));
    }

    @Test
    void shouldExecuteFirmPriceAndUpdateItWhenBidding()
    {
        PnL pnL = new PnL();
        Exchange exchange = exchange(20, (currentTime, security) -> security.midPrice(currentTime, security.midPrice()), pnL);
        exchange.onFirmPriceUpdate(1001, MARKET_MAKER, new FirmPrice(0, 19, 60, 22, 50));

        assertThat(exchange.execute(1002, ARBITRAGEUR, Order.bid(22, 10))).isTrue();

        // Then
        assertThat(exchange.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1002, 19, 60, 22, 40));
        assertThat(pnL.estimatedNominalBalanceOf(MARKET_MAKER)).isEqualTo(20);
        assertThat(pnL.estimatedNominalBalanceOf(ARBITRAGEUR)).isEqualTo(-20);
    }

    @Test
    void shouldExecuteFirmPriceAndUpdateItWhenAsking()
    {
        PnL pnL = new PnL();
        Exchange exchange = exchange(20, (currentTime, security) -> security.midPrice(currentTime, security.midPrice()), pnL);
        exchange.onFirmPriceUpdate(1001, MARKET_MAKER, new FirmPrice(0, 18, 60, 21, 50));

        assertThat(exchange.execute(1002, ARBITRAGEUR, Order.ask(18, 10))).isTrue();

        // Then
        assertThat(exchange.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1002, 18, 50, 21, 50));
        assertThat(pnL.estimatedNominalBalanceOf(MARKET_MAKER)).isEqualTo(20);
        assertThat(pnL.estimatedNominalBalanceOf(ARBITRAGEUR)).isEqualTo(-20);
    }

    @Test
    void shouldKeepTheOriginalFirmPriceWhenFailedToExecute()
    {
        Exchange exchange = exchange(20, new PredictableTrend("trend", 1, 10), new PnL());
        exchange.onFirmPriceUpdate(1001, MARKET_MAKER, new FirmPrice(0, 19, 60, 21, 50));

        assertThat(exchange.execute(1002, ARBITRAGEUR, Order.bid(21, 51))).isFalse();

        // Then
        assertThat(exchange.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(1001, 19, 60, 21, 50));
    }

    @Test
    void shouldSimulateMarketMakerLosingMoneyDueToFirmPriceUpdateDelayInVolatileMarketConditions()
    {
        final int spreadInPips = 10;
        final int lotSize = 1_000_000;
        final int nominalFirmPriceSize = 5 * lotSize;
        final int nominalOrderSize = 2 * lotSize;
        final PnL pnL = new PnL();
        final TimeMachine timeMachine = new TimeMachine();
        final Exchange market = exchange(14500, new PredictableTrend("trend", 3, TimeUnit.MILLISECONDS.toNanos(10)), pnL); // ~ a pip every 10/3 milliseconds (Brexit referendum results anyone?)
        timeMachine.tick(
                market::tick,
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
        assertThat(market.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(4000000, 14490, nominalFirmPriceSize, 14510, nominalFirmPriceSize));
        timeMachine.tick(time -> assertThat(market.execute(time, ARBITRAGEUR, Order.bid(market.midPrice() - 5, nominalOrderSize))).isFalse());

        // MARKET_MAKER has not updated the price for a while as it experiences a GC pause / OS jitter / algo issues - their spread can no longer offset the price movement
        assertThat(market.midPrice()).isEqualTo(14515L);
        int expectedUpdateTimeBeforeOrderMatched = 4000000;
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
        int expectedUpdateTimeAfterOrderMatched = 53000000;
        assertThat(market.firmPrice(MARKET_MAKER)).usingRecursiveComparison().isEqualTo(new FirmPrice(
                expectedUpdateTimeAfterOrderMatched,
                14490,
                nominalFirmPriceSize,
                14510,
                nominalFirmPriceSize - 4 * lotSize
        ));

        // In such volatile conditions, for the spread of 10 pips, 49 ms delay in price updates is enough to lose 20M
        assertThat(expectedNominalAmountTraded).isEqualTo(20_000_000);
        assertThat(expectedUpdateTimeAfterOrderMatched - expectedUpdateTimeBeforeOrderMatched).isEqualTo(TimeUnit.MILLISECONDS.toNanos(49));
    }

    @Test
    void shouldSimulateVolatility()
    {
        final TimeMachine timeMachine = new TimeMachine();
        final TickerSpy midPriceSpy = new TickerSpy();
        final long initialPrice = 0;
        final int priceUpdates = 50_000;

        // When
        final Exchange market = new Exchange(
                new TrackedSecurity().midPrice(0, initialPrice),
                new Volatility(TimeUnit.MILLISECONDS.toNanos(5), 0, singletonList(new RandomizedTrend("moveAround", -50, 100, TimeUnit.MILLISECONDS.toNanos(1)))),
                0L, 0,
                new MarketListenerBuilder().with(midPriceSpy)
        );
        timeMachine.tick(priceUpdates, market::tick);

        // Then
        assertThat(midPriceSpy.midPrices().size()).isEqualTo(priceUpdates);
        long minSeenPrice = midPriceSpy.midPrices().stream().mapToLong(value -> value).min().orElse(Long.MAX_VALUE);
        long maxSeenPrice = midPriceSpy.midPrices().stream().mapToLong(value -> value).max().orElse(Long.MIN_VALUE);
        long lastPrice = midPriceSpy.midPrices().get(midPriceSpy.midPrices().size() - 1);
        assertThat(lastPrice).isCloseTo(initialPrice, Offset.offset((long)priceUpdates));
        assertThat(minSeenPrice).isLessThan(-100);
        assertThat(maxSeenPrice).isGreaterThan(100);
    }

    private Exchange exchange(final long initialPrice, final MidPriceUpdate priceMovement, final ExecutionReportListener executionReportListener)
    {
        return new Exchange(new TrackedSecurity().midPrice(0, initialPrice), priceMovement, 0L, 0, new MarketListenerBuilder().with(executionReportListener));
    }

    static class MarketListenerBuilder implements MarketListener
    {
        private ExecutionReportListener executionReportListener = ExecutionReportListener.IGNORE;
        private TickListener tickListener = TickListener.IGNORE;
        private FirmPriceUpdateListener firmPriceUpdateListener = FirmPriceUpdateListener.IGNORE;
        private OrderResultListener orderResultListener = OrderResultListener.IGNORE;
        private HeartBeatListener heartBeatListener = HeartBeatListener.IGNORE;

        public MarketListenerBuilder with(ExecutionReportListener executionReportListener)
        {
            this.executionReportListener = executionReportListener;
            return this;
        }

        public MarketListenerBuilder with(final TickListener tickListener)
        {
            this.tickListener = tickListener;
            return this;
        }

        public MarketListenerBuilder with(final HeartBeatListener heartBeatListener)
        {
            this.heartBeatListener = heartBeatListener;
            return this;
        }

        public MarketListenerBuilder with(final FirmPriceUpdateListener firmPriceUpdateListener)
        {
            this.firmPriceUpdateListener = firmPriceUpdateListener;
            return this;
        }

        public MarketListenerBuilder with(final OrderResultListener orderResultListener)
        {
            this.orderResultListener = orderResultListener;
            return this;
        }

        @Override
        public void onExecution(final ExecutionReport executionReport)
        {
            executionReportListener.onExecution(executionReport);
        }

        @Override
        public void onFirmPriceUpdate(final int marketMakerId, final FirmPrice firmPrice)
        {
            firmPriceUpdateListener.onFirmPriceUpdate(marketMakerId, firmPrice);
        }

        @Override
        public void onOrderResult(final int marketParticipantId, final OrderResult orderResult)
        {
            orderResultListener.onOrderResult(marketParticipantId, orderResult);
        }

        @Override
        public void onTick(final Security security)
        {
            tickListener.onTick(security);
        }

        @Override
        public void onHeartBeat(final HeartBeat heartBeat)
        {
            heartBeatListener.onHeartBeat(heartBeat);
        }
    }

}