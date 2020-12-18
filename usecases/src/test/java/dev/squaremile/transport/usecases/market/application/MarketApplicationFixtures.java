package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

public class MarketApplicationFixtures
{
    private final ThingsOnDutyRunner onDutyRunner;
    private final MarketMakerApplication marketMakerApplication;
    private final BuySideApplication buySideApplication;
    private final MarketMakerApplication anotherMarketMakerApplication;
    private final BuySideApplication anotherBuySideApplication;

    public MarketApplicationFixtures(final int port, final Clock clock)
    {
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(port, clock);
        final ApplicationStarter<MarketMakerApplication> marketMakerApplicationStarter = marketMakerApplicationStarter(port, clock);
        final ApplicationStarter<BuySideApplication> buySideApplicationStarter = buySideApplicationStarter(port, clock);
        final ApplicationStarter<MarketMakerApplication> anotherMarketMakerApplicationStarter = marketMakerApplicationStarter(port, clock);
        final ApplicationStarter<BuySideApplication> anotherBuySideApplicationStarter = buySideApplicationStarter(port, clock);
        final TransportApplicationOnDuty marketTransportOnDuty = marketApplicationStarter.startTransport(1000);
        final TransportApplicationOnDuty marketMakerTransportOnDuty = marketMakerApplicationStarter.startTransport(marketTransportOnDuty::work, 1000);
        final TransportApplicationOnDuty anotherMarketMakerTransportOnDuty = anotherMarketMakerApplicationStarter.startTransport(marketTransportOnDuty::work, 1000);
        final TransportApplicationOnDuty buySideTransportOnDuty = buySideApplicationStarter.startTransport(marketTransportOnDuty::work, 1000);
        final TransportApplicationOnDuty anotherBuySideTransportOnDuty = anotherBuySideApplicationStarter.startTransport(marketTransportOnDuty::work, 1000);
        onDutyRunner = new ThingsOnDutyRunner(marketTransportOnDuty, marketMakerTransportOnDuty, buySideTransportOnDuty, anotherMarketMakerTransportOnDuty, anotherBuySideTransportOnDuty);
        marketMakerApplication = marketMakerApplicationStarter.application();
        buySideApplication = buySideApplicationStarter.application();
        anotherMarketMakerApplication = anotherMarketMakerApplicationStarter.application();
        anotherBuySideApplication = anotherBuySideApplicationStarter.application();
    }

    private ApplicationStarter<MarketMakerApplication> marketMakerApplicationStarter(final int port, final Clock clock)
    {
        return new ApplicationStarter<>(
                "localhost", port, clock, (connectionTransport, connectionId) -> new MarketMakerApplication(new MarketMakerPublisher(connectionTransport))
        );
    }

    private ApplicationStarter<BuySideApplication> buySideApplicationStarter(final int port, final Clock clock)
    {
        return new ApplicationStarter<>(
                "localhost", port, clock, (connectionTransport, connectionId) -> new BuySideApplication(new BuySidePublisher(connectionTransport))
        );
    }

    public ThingsOnDutyRunner onDutyRunner()
    {
        return onDutyRunner;
    }

    public MarketMakerApplication marketMakerApplication()
    {
        return marketMakerApplication;
    }

    public BuySideApplication buySideApplication()
    {
        return buySideApplication;
    }

    public MarketMakerApplication anotherMarketMakerApplication()
    {
        return anotherMarketMakerApplication;
    }

    public BuySideApplication anotherBuySideApplication()
    {
        return anotherBuySideApplication;
    }
}
