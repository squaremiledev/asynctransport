package dev.squaremile.tcpcheck.ping;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.tcpcheck.probe.Measurements;

import static dev.squaremile.asynctcp.support.transport.FreePort.freePort;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class SourcingConnectionApplicationTest
{
    @Test
    void shouldExchangeMessages() throws InterruptedException
    {
        Measurements measurements = exchangeMessages(
                new TcpPingConfiguration.Builder()
                        .sendingRatePerSecond(100)
                        .respondToNth(4)
                        .secondsRun(1)
                        .secondsWarmUp(1)
                        .extraDataLength(64)
                        .remoteHost("localhost")
                        .remotePort(freePort())
                        .create()
        );

        // Then
        measurements.printResults();
        assertThat(measurements.measurementsCount()).isEqualTo(25);
    }

    @Test
    @Disabled
    void shouldExchangeMessagesAtHighRate() throws InterruptedException
    {
        exchangeMessages(
                new TcpPingConfiguration.Builder()
                        .sendingRatePerSecond(10_000)
                        .respondToNth(32)
                        .secondsWarmUp(40)
                        .secondsRun(10)
                        .extraDataLength(64)
                        .remoteHost("localhost")
                        .remotePort(freePort())
                        .create()
        ).printResults();
    }

    @Test
    @Disabled
    void shouldExchangeMessagesAtHigherRate() throws InterruptedException
    {
        exchangeMessages(
                new TcpPingConfiguration.Builder()
                        .sendingRatePerSecond(400_000)
                        .respondToNth(1000)
                        .secondsWarmUp(40)
                        .secondsRun(10)
                        .extraDataLength(64)
                        .remoteHost("localhost")
                        .remotePort(freePort())
                        .create()
        ).printResults();
    }

    private Measurements exchangeMessages(final TcpPingConfiguration config) throws InterruptedException
    {
        final CountDownLatch applicationReady = new CountDownLatch(1);
        newSingleThreadExecutor().execute(() -> EchoApplication.start(config.remotePort(), applicationReady::countDown));
        applicationReady.await();
        return SourcingConnectionApplication.runPing(config);
    }

}