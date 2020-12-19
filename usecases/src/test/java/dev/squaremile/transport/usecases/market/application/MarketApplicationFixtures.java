package dev.squaremile.transport.usecases.market.application;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.transport.usecases.market.domain.MidPriceUpdate;
import dev.squaremile.transport.usecases.market.domain.RandomizedTrend;
import dev.squaremile.transport.usecases.market.domain.Volatility;

public class MarketApplicationFixtures
{
    private final ThingsOnDutyRunner onDutyRunner;
    private final MarketMakerApplication marketMakerApplication;
    private final BuySideApplication buySideApplication;
    private final MarketMakerApplication anotherMarketMakerApplication;
    private final BuySideApplication anotherBuySideApplication;
    private final MarketMakerChart chart;

    public MarketApplicationFixtures(final int port, final Clock clock)
    {
        final MidPriceUpdate priceMovement = new Volatility(new CustomTrendSetter(
                TimeUnit.MILLISECONDS.toNanos(500),
                Arrays.asList(
                        new RandomizedTrend("random", 100, 1, TimeUnit.MICROSECONDS.toNanos(100)),
                        new RandomizedTrend("random", -100, 1, TimeUnit.MICROSECONDS.toNanos(100))
                )
        ));
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(
                port, clock, TimeUnit.MICROSECONDS.toNanos(50), priceMovement, 0);
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
        chart = marketApplicationStarter.chart();
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

    public MarketMakerChart chart()
    {
        return chart;
    }
}
