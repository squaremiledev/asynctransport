package dev.squaremile.transport.casestudy.marketmaking.application;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.transport.casestudy.marketmaking.domain.ExecutionReport;
import dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice;
import dev.squaremile.transport.casestudy.marketmaking.domain.MidPriceUpdate;
import dev.squaremile.transport.casestudy.marketmaking.domain.Order;
import dev.squaremile.transport.casestudy.marketmaking.domain.OrderResult;
import dev.squaremile.transport.casestudy.marketmaking.domain.RandomizedTrend;
import dev.squaremile.transport.casestudy.marketmaking.domain.Volatility;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static dev.squaremile.transport.casestudy.marketmaking.domain.CurrentTime.currentTime;
import static dev.squaremile.transport.casestudy.marketmaking.domain.CurrentTime.timeFromMs;
import static dev.squaremile.transport.casestudy.marketmaking.domain.MarketListener.MarketMessageListener.IGNORE;
import static java.util.Collections.singletonList;

class ExchangeApplicationTest
{
    private final MarketMakerApplication anotherMarketMakerApplication;
    private final BuySideApplication anotherBuySideApplication;
    private final ThingsOnDutyRunner onDutyRunner;
    private final MarketMakerApplication marketMakerApplication;
    private final BuySideApplication buySideApplication;

    public ExchangeApplicationTest()
    {
        final MidPriceUpdate priceMovement = new Volatility(
                TimeUnit.MINUTES.toNanos(500),
                TimeUnit.MILLISECONDS.toNanos(300),
                singletonList(new RandomizedTrend("trend", -10, 20, TimeUnit.MICROSECONDS.toNanos(500)))
        );
        final MarketMakerChart chart = new MarketMakerChart(TimeUnit.NANOSECONDS::toMicros, 300);
        final Clock clock = new Clock();
        final int port = freePort();
        final ExchangeApplicationStarter exchangeApplicationStarter = new ExchangeApplicationStarter(port, clock, TimeUnit.MICROSECONDS.toNanos(50), priceMovement, 1000, chart);
        final ApplicationStarter<MarketMakerApplication> marketMakerApplicationStarter = new ApplicationStarter<>(
                "localhost",
                port,
                clock,
                (connectionTransport, connectionId) -> new MarketMakerApplication(new MarketMessagePublisher(connectionTransport), IGNORE)
        );
        final ApplicationStarter<MarketMakerApplication> anotherMarketMakerApplicationStarter = new ApplicationStarter<>(
                "localhost",
                port,
                clock,
                (connectionTransport, connectionId) -> new MarketMakerApplication(new MarketMessagePublisher(connectionTransport), IGNORE)
        );
        final ApplicationStarter<BuySideApplication> buySideApplicationStarter = new ApplicationStarter<>(
                "localhost",
                port,
                clock,
                (connectionTransport, connectionId) -> new BuySideApplication(new BuySidePublisher(connectionTransport))
        );
        final ApplicationStarter<BuySideApplication> anotherBuySideApplicationStarter = new ApplicationStarter<>(
                "localhost",
                port,
                clock,
                (connectionTransport, connectionId) -> new BuySideApplication(new BuySidePublisher(connectionTransport))
        );

        final TransportApplicationOnDuty marketTransportOnDuty = exchangeApplicationStarter.startTransport(1000);
        onDutyRunner = new ThingsOnDutyRunner(
                marketTransportOnDuty,
                marketMakerApplicationStarter.startTransport(marketTransportOnDuty::work, 1000),
                anotherMarketMakerApplicationStarter.startTransport(marketTransportOnDuty::work, 1000),
                buySideApplicationStarter.startTransport(marketTransportOnDuty::work, 1000),
                anotherBuySideApplicationStarter.startTransport(marketTransportOnDuty::work, 1000)
        );
        marketMakerApplication = marketMakerApplicationStarter.application();
        buySideApplication = buySideApplicationStarter.application();
        anotherMarketMakerApplication = anotherMarketMakerApplicationStarter.application();
        anotherBuySideApplication = anotherBuySideApplicationStarter.application();
    }

    @Test
    void shouldInformMarketMakerAboutSuccessfulPriceUpdate()
    {
        // Given
        assertThat(marketMakerApplication.acknowledgedPriceUpdatesCount()).isEqualTo(0);

        // When
        final long updateTime = currentTime();
        marketMakerApplication.updatePrice(new FirmPrice(5, updateTime, 99, 40, 101, 50));
        runUntil(onDutyRunner.reached(() -> marketMakerApplication.acknowledgedPriceUpdatesCount() > 0));

        // Then
        assertThat(marketMakerApplication.acknowledgedPriceUpdatesCount()).isEqualTo(1);
        FirmPrice lastUpdatedFirmPrice = marketMakerApplication.lastUpdatedFirmPrice();
        assertThat(lastUpdatedFirmPrice).usingRecursiveComparison()
                .isEqualTo(new FirmPrice(5, lastUpdatedFirmPrice.updateTime(), 99, 40, 101, 50));
        assertThat(lastUpdatedFirmPrice.updateTime()).isGreaterThanOrEqualTo(updateTime);
        assertThat(lastUpdatedFirmPrice.updateTime()).isLessThan(updateTime + timeFromMs(100));
    }

    @Test
    void shouldInformAggressorAboutOrderFailure()
    {
        assertThat(buySideApplication.orderResultCount()).isEqualTo(0);
        assertThat(buySideApplication.lastOrderResult()).isNull();

        buySideApplication.sendOrder(Order.bid(100, 50));
        runUntil(onDutyRunner.reached(() -> buySideApplication.orderResultCount() > 0));

        assertThat(buySideApplication.orderResultCount()).isEqualTo(1);
        assertThat(buySideApplication.lastOrderResult()).usingRecursiveComparison().isEqualTo(OrderResult.NOT_EXECUTED);
        assertThat(marketMakerApplication.executedReportsCount()).isEqualTo(0);
    }

    @Test
    void shouldInformAggressorAndMarketMakerAboutOrderExecution()
    {
        // Given
        marketMakerApplication.updatePrice(new FirmPrice(5, currentTime(), 99, 40, 101, 50));
        runUntil(onDutyRunner.reached(() -> marketMakerApplication.acknowledgedPriceUpdatesCount() == 1));

        // When
        buySideApplication.sendOrder(Order.ask(99, 30));
        runUntil(onDutyRunner.reached(() -> buySideApplication.orderResultCount() > 0 &&
                                            marketMakerApplication.executedReportsCount() > 0 &&
                                            buySideApplication.executedReportsCount() > 0
        ));

        // Then
        assertThat(buySideApplication.orderResultCount()).isEqualTo(1);
        assertThat(buySideApplication.lastOrderResult()).usingRecursiveComparison().isEqualTo(OrderResult.EXECUTED);
        assertThat(buySideApplication.executedReportsCount()).isEqualTo(1);
        ExecutionReport actualBuySideExecutionReport = buySideApplication.lastExecutedOrder();
        assertThat(actualBuySideExecutionReport).usingRecursiveComparison().isEqualTo(
                new ExecutionReport().update(0, 2, actualBuySideExecutionReport.security(), Order.ask(99, 30))
        );
        assertThat(marketMakerApplication.executedReportsCount()).isEqualTo(1);
        ExecutionReport actualMarketMakerExecutionReport = marketMakerApplication.lastExecutedOrder();
        assertThat(actualMarketMakerExecutionReport).usingRecursiveComparison().isEqualTo(
                new ExecutionReport().update(0, 2, actualMarketMakerExecutionReport.security(), Order.ask(99, 30))
        );
    }

    @Test
    void shouldNotInformNotInvolvedMarketParticipantsAboutOrderExecution()
    {
        marketMakerApplication.updatePrice(new FirmPrice(5, currentTime(), 99, 40, 101, 50));
        runUntil(onDutyRunner.reached(() -> marketMakerApplication.acknowledgedPriceUpdatesCount() == 1));
        assertThat(anotherMarketMakerApplication.acknowledgedPriceUpdatesCount()).isEqualTo(0);

        // When
        buySideApplication.sendOrder(Order.ask(99, 30));
        runUntil(onDutyRunner.reached(() -> buySideApplication.orderResultCount() > 0 &&
                                            marketMakerApplication.executedReportsCount() > 0 &&
                                            buySideApplication.executedReportsCount() > 0
        ));

        // Then
        assertThat(anotherBuySideApplication.orderResultCount()).isEqualTo(0);
        assertThat(anotherBuySideApplication.executedReportsCount()).isEqualTo(0);
        assertThat(anotherMarketMakerApplication.executedReportsCount()).isEqualTo(0);
    }

    @Test
    void shouldSendMidPriceUpdates()
    {
        runUntil(onDutyRunner.reached(() -> buySideApplication.midPriceUpdatesCount() > 0 &&
                                            marketMakerApplication.midPriceUpdatesCount() > 0));
    }
}