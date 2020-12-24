package dev.squaremile.transport.usecases.market.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.Order;
import dev.squaremile.transport.usecases.market.domain.OrderResult;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static dev.squaremile.transport.usecases.market.domain.CurrentTime.currentTime;
import static dev.squaremile.transport.usecases.market.domain.CurrentTime.timeFromMs;

class MarketApplicationTest
{
    private final Clock clock = new Clock();
    private final MarketApplicationFixtures fixtures = new MarketApplicationFixtures(freePort(), clock);
    private final ThingsOnDutyRunner onDutyRunner = fixtures.onDutyRunner();
    private final MarketMakerApplication marketMakerApplication = fixtures.marketMakerApplication();
    private final BuySideApplication buySideApplication = fixtures.buySideApplication();


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
        assertThat(fixtures.anotherMarketMakerApplication().acknowledgedPriceUpdatesCount()).isEqualTo(0);

        // When
        buySideApplication.sendOrder(Order.ask(99, 30));
        runUntil(onDutyRunner.reached(() -> buySideApplication.orderResultCount() > 0 &&
                                            marketMakerApplication.executedReportsCount() > 0 &&
                                            buySideApplication.executedReportsCount() > 0
        ));

        // Then
        assertThat(fixtures.anotherBuySideApplication().orderResultCount()).isEqualTo(0);
        assertThat(fixtures.anotherBuySideApplication().executedReportsCount()).isEqualTo(0);
        assertThat(fixtures.anotherMarketMakerApplication().executedReportsCount()).isEqualTo(0);
    }

    @Test
    void shouldSendMidPriceUpdates()
    {
        runUntil(onDutyRunner.reached(() -> buySideApplication.midPriceUpdatesCount() > 0 &&
                                            marketMakerApplication.midPriceUpdatesCount() > 0));
    }
}