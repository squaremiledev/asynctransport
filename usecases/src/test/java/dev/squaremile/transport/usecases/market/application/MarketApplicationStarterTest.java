package dev.squaremile.transport.usecases.market.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class MarketApplicationStarterTest
{
    @Test
    void shouldAcceptMarketMakerConnection()
    {
        final int port = freePort();
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(port);
        final MarketMakerApplicationStarter marketMakerApplicationStarter = new MarketMakerApplicationStarter("localhost", port);

        // Given
        assertThat(marketApplicationStarter.marketTransportApplication()).isNull();
        assertThat(marketMakerApplicationStarter.marketMakerTransportApplication()).isNull();

        // When
        TransportApplicationOnDuty marketTransportOnDuty = marketApplicationStarter.startTransport(1000);
        marketMakerApplicationStarter.startTransport(marketTransportOnDuty::work, 1000);

        // Then
        assertThat(marketApplicationStarter.marketTransportApplication()).isNotNull();
        assertThat(marketMakerApplicationStarter.marketMakerTransportApplication()).isNotNull();
    }
}