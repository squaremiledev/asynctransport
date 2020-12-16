package dev.squaremile.transport.usecases.market.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.testfixtures.Worker;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;

class MarketApplicationTest
{
    private final int port = freePort();
    private final MarketApplication marketApplication = new MarketApplication(port);
    private final MarketMakerApplication marketMakerApplication = new MarketMakerApplication("localhost", port);

    @Test
    void shouldAcceptMarketMakerConnection()
    {
        assertThat(marketApplication.startedListening()).isFalse();
        assertThat(marketApplication.acceptedMarketMakerConnection()).isFalse();

        marketApplication.startTransport(marketApplication::startedListening, 1000);
        marketMakerApplication.startTransport(marketApplication::acceptedMarketMakerConnection, 1000);

        assertThat(marketApplication.startedListening()).isTrue();
        assertThat(marketApplication.acceptedMarketMakerConnection()).isTrue();
    }

    @Test
    void shouldConfirmReceiptOfThePriceUpdate()
    {
        ThingsOnDutyRunner onDuty = new ThingsOnDutyRunner(
                marketApplication.startTransport(marketApplication::startedListening, 1000),
                marketMakerApplication.startTransport(marketApplication::acceptedMarketMakerConnection, 1000)
        );
        assertThat(marketMakerApplication.acknowledgedPriceUpdatesCount()).isEqualTo(0);
        marketMakerApplication.updatePrice(new FirmPrice(now(), 99, 40, 101, 50));
        runUntil(onDuty.reached(() -> marketMakerApplication.acknowledgedPriceUpdatesCount() == 1));
    }

    private long now()
    {
        return System.currentTimeMillis();
    }
}