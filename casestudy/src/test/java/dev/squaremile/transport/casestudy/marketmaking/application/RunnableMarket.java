package dev.squaremile.transport.casestudy.marketmaking.application;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.agrona.LangUtil.rethrowUnchecked;


import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.transport.casestudy.marketmaking.domain.MidPriceUpdate;
import dev.squaremile.transport.casestudy.marketmaking.domain.RandomizedTrend;
import dev.squaremile.transport.casestudy.marketmaking.domain.Volatility;

import static java.util.Collections.singletonList;

class RunnableMarket implements Runnable
{
    private final int port;
    private final Clock clock;
    private final MidPriceUpdate priceMovement;
    private final CountDownLatch onReady;
    private final MarketMakerChart chart;
    private final long initialDelay;

    public RunnableMarket(final int port, final long initialDelay)
    {
        this.clock = new Clock();
        this.priceMovement = new Volatility(
                TimeUnit.MINUTES.toNanos(500),
                0,
                singletonList(new RandomizedTrend("trend", -10, 20, TimeUnit.MICROSECONDS.toNanos(500)))
        );
        this.port = port;
        this.chart = new MarketMakerChart(TimeUnit.NANOSECONDS::toMicros, 300);
        this.onReady = new CountDownLatch(1);
        this.initialDelay = initialDelay;
    }

    public int port()
    {
        return port;
    }

    @Override
    public void run()
    {
        final TransportApplicationOnDuty marketTransportOnDuty = new ExchangeApplicationStarter(
                port, clock, initialDelay, TimeUnit.MICROSECONDS.toNanos(50), priceMovement, 1000, chart
        ).startTransport(1000);
        onReady.countDown();

        while (true)
        {
            marketTransportOnDuty.work();
        }
    }

    void awaitReady() throws InterruptedException
    {
        onReady.await();
    }

    byte[] performanceChartContent()
    {
        return chart.generateAsStringBytes();
    }

    public long messagesCount()
    {
        return chart.messagesCount();
    }

    public RunnableMarket runInSeparateThread()
    {
        if (onReady.getCount() < 1)
        {
            throw new IllegalStateException("Already started");
        }

        try
        {
            Executors.newSingleThreadExecutor().execute(this);
            awaitReady();
        }
        catch (InterruptedException e)
        {
            rethrowUnchecked(e);
        }
        return this;
    }
}
