package dev.squaremile.transport.usecases.market.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

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

}