package dev.squaremile.transport.usecases.market.application;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.transport.usecases.market.domain.PredictableTrend;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class ApplicationStarterTest
{
    @Test
    void shouldAcceptMarketMakerConnection()
    {
        final int port = freePort();
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(port, new Clock(), 0, new PredictableTrend("trend", 1, 1), 1_000_000,
                                                                                               new MarketMakerChart(TimeUnit.NANOSECONDS::toMillis, 300)
        );
        final ApplicationStarter<MarketMakerApplication> applicationStarter = new ApplicationStarter<>(
                "localhost", port, new Clock(), (connectionTransport, connectionId) -> new MarketMakerApplication(new MarketMakerPublisher(connectionTransport), marketMessage ->
        {
        }));

        // Given
        assertThat(marketApplicationStarter.application()).isNull();
        assertThat(applicationStarter.application()).isNull();

        // When
        TransportApplicationOnDuty marketTransportOnDuty = marketApplicationStarter.startTransport(1000);
        applicationStarter.startTransport(marketTransportOnDuty::work, 1000);

        // Then
        assertThat(marketApplicationStarter.application()).isNotNull();
        assertThat(applicationStarter.application()).isNotNull();
    }
}