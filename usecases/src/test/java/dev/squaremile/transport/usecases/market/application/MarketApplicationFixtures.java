package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

public class MarketApplicationFixtures
{
    private final ThingsOnDutyRunner onDutyRunner;
    private final MarketTransportApplication marketTransportApplication;
    private final MarketMakerTransportApplication marketMakerTransportApplication;

    public MarketApplicationFixtures(final int port)
    {
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(port);
        final MarketMakerApplicationStarter marketMakerApplicationStarterFactory = new MarketMakerApplicationStarter("localhost", port);
        final TransportApplicationOnDuty marketTransportOnDuty = marketApplicationStarter.startTransport(1000);
        final TransportApplicationOnDuty marketMakerTransportOnDuty = marketMakerApplicationStarterFactory.startTransport(marketTransportOnDuty::work, 1000);
        onDutyRunner = new ThingsOnDutyRunner(marketTransportOnDuty, marketMakerTransportOnDuty);
        marketTransportApplication = marketApplicationStarter.marketTransportApplication();
        marketMakerTransportApplication = marketMakerApplicationStarterFactory.marketMakerTransportApplication();
    }

    public ThingsOnDutyRunner onDutyRunner()
    {
        return onDutyRunner;
    }

    public MarketTransportApplication marketTransportApplication()
    {
        return marketTransportApplication;
    }

    public MarketMakerTransportApplication marketMakerTransportApplication()
    {
        return marketMakerTransportApplication;
    }
}
