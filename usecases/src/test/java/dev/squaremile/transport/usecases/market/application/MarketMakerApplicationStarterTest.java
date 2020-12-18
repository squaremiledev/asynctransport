package dev.squaremile.transport.usecases.market.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class MarketMakerApplicationStarterTest
{
    @Test
    void shouldAcceptMarketMakerConnection()
    {
        final int port = freePort();
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(port, new Clock());
        final MarketMakerApplicationStarter marketMakerApplicationStarter = new MarketMakerApplicationStarter("localhost", port, new Clock());

        // Given
        assertThat(marketApplicationStarter.application()).isNull();
        assertThat(marketMakerApplicationStarter.application()).isNull();

        // When
        TransportApplicationOnDuty marketTransportOnDuty = marketApplicationStarter.startTransport(1000);
        marketMakerApplicationStarter.startTransport(marketTransportOnDuty::work, 1000);

        // Then
        assertThat(marketApplicationStarter.application()).isNotNull();
        assertThat(marketMakerApplicationStarter.application()).isNotNull();
    }
}