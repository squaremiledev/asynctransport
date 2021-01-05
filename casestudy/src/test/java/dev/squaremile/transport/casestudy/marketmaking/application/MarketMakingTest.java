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
        final RunnableMarket runnableMarket = new RunnableMarket(freePort(), TimeUnit.SECONDS.toNanos(30), TimeUnit.MICROSECONDS.toNanos(10)).runInSeparateThread();
        final ApplicationStarter<SpreadMarketMaking> marketMakerApplicationStarter = new ApplicationStarter<>(
                "localhost", runnableMarket.port(), new Clock(), (connectionTransport, connectionId) -> new SpreadMarketMaking(new MarketMessagePublisher(connectionTransport), 100));
        final TransportApplicationOnDuty marketMakerOnDuty = marketMakerApplicationStarter.startTransport(1000);
        SpreadMarketMaking marketMaker = marketMakerApplicationStarter.application();
        spin(ofSeconds(35), marketMakerOnDuty);
        long messagesReceivedCountAtStart = marketMaker.receivedMessagesCount();
        long marketSentMessagesCountAtStart = runnableMarket.messagesCount();
        final Instant startTime = Instant.now();
        long iterations = spin(ofSeconds(5), marketMakerOnDuty);
        final Instant endTime = Instant.now();
        long marketSentMessagesCount = runnableMarket.messagesCount();
        long messagesReceivedCount = marketMaker.receivedMessagesCount();

        System.out.printf(
                "Run in steady state for %s performing %d iterations per second, sending %d and receiving %d messages %n",
                between(startTime, endTime).toString(),
                iterations / between(startTime, endTime).getSeconds(),
                marketSentMessagesCount - marketSentMessagesCountAtStart,
                messagesReceivedCount - messagesReceivedCountAtStart
        );
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/index.html"), ChartTemplate.chartRendering().getBytes());
        Files.write(Paths.get("/tmp/asynctransportmarketmaking/data.txt"), runnableMarket.performanceChartContent());
    }
}