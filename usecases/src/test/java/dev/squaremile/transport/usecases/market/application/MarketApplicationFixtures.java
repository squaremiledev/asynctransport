package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

public class MarketApplicationFixtures
{
    private final ThingsOnDutyRunner onDutyRunner;
    private final MarketMakerApplication marketMakerApplication;

    public MarketApplicationFixtures(final int port, final MarketMakerTransportApplication.MarketMakerApplicationFactory makerApplicationFactory)
    {
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(port);
        final MarketMakerApplicationStarter marketMakerApplicationStarterFactory = new MarketMakerApplicationStarter("localhost", port, makerApplicationFactory);
        final TransportApplicationOnDuty marketTransportOnDuty = marketApplicationStarter.startTransport(1000);
        final TransportApplicationOnDuty marketMakerTransportOnDuty = marketMakerApplicationStarterFactory.startTransport(marketTransportOnDuty::work, 1000);
        onDutyRunner = new ThingsOnDutyRunner(marketTransportOnDuty, marketMakerTransportOnDuty);
        marketMakerApplication = marketMakerApplicationStarterFactory.marketMakerApplication();
    }

    public ThingsOnDutyRunner onDutyRunner()
    {
        return onDutyRunner;
    }

    public MarketMakerApplication marketMakerApplication()
    {
        return marketMakerApplication;
    }
}
