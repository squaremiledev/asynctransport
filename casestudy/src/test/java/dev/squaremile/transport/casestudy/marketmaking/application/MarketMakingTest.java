package dev.squaremile.transport.casestudy.marketmaking.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.transport.api.app.OnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.time.Duration.between;
import static java.time.Duration.ofSeconds;

@Disabled
class MarketMakingTest
{
    private static long spin(final Duration duration, final OnDuty thingOnDuty)
    {
        final long startMs = System.currentTimeMillis();
        long deadlineCheckMs = startMs;
        long iteration = 0;
        while (true)
        {
            if (iteration++ % 10_000 == 0)
            {
                long nowMs = System.currentTimeMillis();
                if (nowMs > deadlineCheckMs)
                {
                    if (nowMs - startMs > duration.toMillis())
                    {
                        break;
                    }
                    deadlineCheckMs = nowMs + TimeUnit.SECONDS.toMillis(1);
                }
            }
            thingOnDuty.work();
        }
        return iteration;
    }

    @Test
    void runSimulation() throws IOException
    {
        final RunnableMarket runnableMarket = new RunnableMarket(freePort(), TimeUnit.SECONDS.toNanos(3)).runInSeparateThread();
        final ApplicationStarter<SpreadMarketMaking> marketMakerApplicationStarter = new ApplicationStarter<>(
                "localhost", runnableMarket.port(), new Clock(), (connectionTransport, connectionId) -> new SpreadMarketMaking(new MarketMessagePublisher(connectionTransport), 100, 0));
        final TransportApplicationOnDuty marketMakerOnDuty = marketMakerApplicationStarter.startTransport(1000);
        spin(ofSeconds(5), marketMakerOnDuty);

        final Instant startTime = Instant.now();
        long iterations = spin(ofSeconds(5), marketMakerOnDuty);
        final Instant endTime = Instant.now();

        System.out.printf(
                "Run in steady state for %s performing %d iterations per second%n",
                between(startTime, endTime).toString(),
                iterations / between(startTime, endTime).getSeconds()
        );
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/index.html"), ChartTemplate.chartRendering().getBytes());
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/data.txt"), runnableMarket.performanceChartContent());
    }
}