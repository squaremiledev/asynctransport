package dev.squaremile.transport.casestudy.marketmaking.application;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketListener;
import dev.squaremile.transport.casestudy.marketmaking.domain.PredictableTrend;

import static dev.squaremile.asynctcp.support.transport.FreePort.freePort;

class ApplicationStarterTest
{
    @Test
    void shouldAcceptMarketMakerConnection()
    {
        final int port = freePort();
        final ExchangeApplicationStarter exchangeApplicationStarter = new ExchangeApplicationStarter(port, new Clock(), 0L, 0, new PredictableTrend("trend", 1, 1), 1_000_000,
                                                                                                     new MarketMakerChart(TimeUnit.NANOSECONDS::toMillis, 300)
        );
        final ApplicationStarter<MarketMakerApplication> applicationStarter = new ApplicationStarter<>(
                "localhost",
                port,
                new Clock(),
                (connectionTransport, connectionId) -> new MarketMakerApplication(new MarketMessagePublisher(connectionTransport), MarketListener.MarketMessageListener.IGNORE)
        );

        // Given
        assertThat(applicationStarter.application()).isNull();

        // When
        TransportApplicationOnDuty marketTransportOnDuty = exchangeApplicationStarter.startTransport(1000);
        applicationStarter.startTransport(marketTransportOnDuty::work, 1000);

        // Then
        assertThat(applicationStarter.application()).isNotNull();
    }
}