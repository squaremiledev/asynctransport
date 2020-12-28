package dev.squaremile.transport.usecases.market.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.transport.usecases.market.domain.MidPriceUpdate;
import dev.squaremile.transport.usecases.market.domain.RandomizedTrend;
import dev.squaremile.transport.usecases.market.domain.Volatility;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static java.util.Collections.singletonList;

@Disabled
class MarketMakingTest
{
    @Test
    void runSimulation() throws IOException
    {
        final int port = freePort();
        final Clock clock = new Clock();

        final MidPriceUpdate priceMovement = new Volatility(
                TimeUnit.MINUTES.toNanos(500),
                TimeUnit.MILLISECONDS.toNanos(300),
                singletonList(new RandomizedTrend("trend", -10, 20, TimeUnit.MICROSECONDS.toNanos(500)))
        );
        final MarketMakerChart chart = new MarketMakerChart(TimeUnit.NANOSECONDS::toMicros, 300);
        final MarketApplicationStarter marketApplicationStarter = new MarketApplicationStarter(port, clock, TimeUnit.MICROSECONDS.toNanos(50), priceMovement, 1000, chart);
        final ApplicationStarter<MarketMakerApplication> marketMakerApplicationStarter = new ApplicationStarter<>("localhost", port, clock, (connectionTransport, connectionId) ->
                new MarketMakerApplication(new MarketMakerPublisher(connectionTransport))
        );

        final int spread = 100;
        final MutableLong correlationId = new MutableLong(0);
        final TransportApplicationOnDuty marketTransportOnDuty = marketApplicationStarter.startTransport(1000);
        final TransportApplicationOnDuty marketMakerTransportOnDuty = marketMakerApplicationStarter.startTransport(marketTransportOnDuty::work, 1000);
        final MarketMakerApplication marketMakerApplication = marketMakerApplicationStarter.application();
        marketMakerApplication.configureOnSecurityUpdate(security -> marketMakerApplication.marketMakerPublisher().publish(
                marketMakerApplication.firmPricePublication().update(
                        correlationId.incrementAndGet(), security.lastUpdateTime(), security.midPrice() - spread, 100, security.midPrice() + spread, 100)));
        final ThingsOnDutyRunner onDutyRunner = new ThingsOnDutyRunner(marketTransportOnDuty, marketMakerTransportOnDuty);
        runUntil(5000, onDutyRunner.reached(() -> marketMakerApplication.midPriceUpdatesCount() > 1000));
        final long iterations = 1_000_000;
        int i = 0;
        long beforeMs = System.currentTimeMillis();
        while (!Thread.interrupted() && i++ < iterations)
        {
            onDutyRunner.work();
        }
        long afterMs = System.currentTimeMillis();
        long steadyStateTimeElapsedMs = afterMs - beforeMs;
        long iterationsPerSecond = iterations * 1000 / steadyStateTimeElapsedMs;
        System.out.println("steadyStateTimeElapsedMs = " + Duration.ofMillis(steadyStateTimeElapsedMs));
        System.out.println("iterationsPerSecond = " + iterationsPerSecond);
        System.out.println("entries captured = " + chart.entriesCaptured());
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/index.html"), ChartTemplate.chartRendering().getBytes());
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/data.txt"), chart.generateAsStringBytes());
    }
}