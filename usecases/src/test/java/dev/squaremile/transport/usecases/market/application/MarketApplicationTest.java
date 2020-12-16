package dev.squaremile.transport.usecases.market.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static java.lang.System.currentTimeMillis;

class MarketApplicationTest
{
    private final MarketApplicationFixtures fixtures = new MarketApplicationFixtures(freePort(), MarketMakerApplication::new);
    private final ThingsOnDutyRunner onDutyRunner = fixtures.onDutyRunner();
    private final MarketMakerApplication marketMakerApplication = fixtures.marketMakerApplication();

    @Test
    void shouldConfirmReceiptOfThePriceUpdate()
    {
        // Given
        assertThat(marketMakerApplication.acknowledgedPriceUpdatesCount()).isEqualTo(0);

        // When
        marketMakerApplication.updatePrice(new FirmPrice(1, currentTimeMillis(), 99, 40, 101, 50));
        runUntil(onDutyRunner.reached(() -> marketMakerApplication.acknowledgedPriceUpdatesCount() > 0));

        // Then
        assertThat(marketMakerApplication.acknowledgedPriceUpdatesCount()).isEqualTo(1);
    }
}