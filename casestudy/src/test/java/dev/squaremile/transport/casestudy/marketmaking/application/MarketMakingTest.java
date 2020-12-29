package dev.squaremile.transport.casestudy.marketmaking.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;

@Disabled
class MarketMakingTest
{
    private final RunnableMarket runnableMarket = new RunnableMarket(freePort()).runInSeparateThread();

    @Test
    void runSimulation() throws IOException
    {
        final ApplicationStarter<SpreadMarketMaking> marketMakerApplicationStarter = new ApplicationStarter<>(
                "localhost", runnableMarket.port(), new Clock(), (connectionTransport, connectionId) -> new SpreadMarketMaking(new MarketMakerPublisher(connectionTransport), 100, 20_000));
        final TransportApplicationOnDuty marketMakerTransportOnDuty = marketMakerApplicationStarter.startTransport(1000);
        final SpreadMarketMaking marketMakerApplication = marketMakerApplicationStarter.application();
        final ThingsOnDutyRunner onDutyRunner = new ThingsOnDutyRunner(marketMakerTransportOnDuty);
        runUntil(10_000, onDutyRunner.reached(marketMakerApplication::hasWarmedUp));
        final long beforeMs = System.currentTimeMillis();
        long deadlineCheckMs = beforeMs;
        long iteration = 0;
        while (true)
        {
            if (iteration++ % 10_000 == 0)
            {
                long nowMs = System.currentTimeMillis();
                if (nowMs > deadlineCheckMs)
                {
                    if (nowMs - beforeMs > TimeUnit.SECONDS.toMillis(2))
                    {
                        break;
                    }
                    deadlineCheckMs = nowMs + TimeUnit.SECONDS.toMillis(1);
                }
            }
            onDutyRunner.work();
        }
        long afterMs = System.currentTimeMillis();
        long steadyStateTimeElapsedMs = afterMs - beforeMs;
        long iterationsPerSecond = iteration * 1000 / steadyStateTimeElapsedMs;
        System.out.println("steadyStateTimeElapsedMs = " + Duration.ofMillis(steadyStateTimeElapsedMs));
        System.out.println("iterationsPerSecond = " + iterationsPerSecond);
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/index.html"), ChartTemplate.chartRendering().getBytes());
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/data.txt"), runnableMarket.performanceChartContent());
    }

}