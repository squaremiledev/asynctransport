package dev.squaremile.trcheck.standalone;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.trcheck.probe.Measurements;

import static dev.squaremile.asynctcp.support.transport.FreePort.freePort;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class SourcingConnectionApplicationTest
{

    public static final int METADATA_SIZE_IN_BYTES = 24;

    @Test
    void shouldExchangeMessages() throws InterruptedException
    {
        int sendingRatePerSecond = 100;
        int secondsRun = 2;
        int secondsWarmUp = 1;
        int extraData = 64;
        int respondToNth = 4;
        int tcpDataPerMessage = METADATA_SIZE_IN_BYTES + extraData;

        Measurements measurements = exchangeMessages(
                new TcpPingConfiguration.Builder()
                        .sendingRatePerSecond(sendingRatePerSecond)
                        .respondToNth(respondToNth)
                        .secondsRun(secondsRun)
                        .secondsWarmUp(secondsWarmUp)
                        .extraDataLength(extraData)
                        .remoteHost("localhost")
                        .remotePort(freePort())
                        .create()
        );

        // Then
        measurements.printResults();
        assertThat(measurements.measurementsCount()).isEqualTo(50);
        assertThat(measurements.messagesSentCount()).isCloseTo(
                (secondsWarmUp + secondsRun) * sendingRatePerSecond,
                Offset.offset((long)respondToNth)
        );
        assertThat(measurements.dataSentInBytes()).isCloseTo(
                (secondsWarmUp + secondsRun) * sendingRatePerSecond * tcpDataPerMessage,
                Offset.offset((long)respondToNth * tcpDataPerMessage)
        );
        assertThat(measurements.averageThroughputMbps()).isCloseTo(
                (double)((measurements.dataSentInBytes() * 8 * 1000 / measurements.sendingTimeInMs())) / 1_000_000,
                Offset.offset(0.01)
        );
        assertThat(measurements.averageSentMessageSizeInBytes()).isEqualTo(METADATA_SIZE_IN_BYTES + extraData);
    }

    @Test
    @Disabled
    void shouldExchangeMillionsOfMessagesASecondWithAcceptableLatency() throws InterruptedException
    {
        exchangeMessages(
                new TcpPingConfiguration.Builder()
                        .sendingRatePerSecond(4_500_000)
                        .respondToNth(4500)
                        .secondsWarmUp(30)
                        .secondsRun(20)
                        .extraDataLength(0)
                        .remoteHost("localhost")
                        .remotePort(freePort())
                        .useRingBuffers(false)
                        .create()
        ).printResults();
    }

    @Test
    @Disabled
    void shouldExchangeManyMessagesASecondWithAcceptableLatency() throws InterruptedException
    {
        exchangeMessages(
                new TcpPingConfiguration.Builder()
                        .sendingRatePerSecond(600_000)
                        .respondToNth(600)
                        .secondsWarmUp(30)
                        .secondsRun(20)
                        .extraDataLength(0)
                        .remoteHost("localhost")
                        .remotePort(freePort())
                        .useRingBuffers(false)
                        .create()
        ).printResults();
    }

    @Test
    @Disabled
    void shouldExchangeMessagesAtHigherRateUsingRingBuffers() throws InterruptedException
    {
        exchangeMessages(
                new TcpPingConfiguration.Builder()
                        .sendingRatePerSecond(600_000)
                        .respondToNth(600)
                        .secondsWarmUp(30)
                        .secondsRun(20)
                        .extraDataLength(0)
                        .remoteHost("localhost")
                        .remotePort(freePort())
                        .useRingBuffers(true)
                        .create()
        ).printResults();
    }

    private Measurements exchangeMessages(final TcpPingConfiguration config) throws InterruptedException
    {
        final CountDownLatch applicationReady = new CountDownLatch(1);
        final CountDownLatch applicationShutDown = new CountDownLatch(1);
        ExecutorService executorService = newSingleThreadExecutor();
        executorService.execute(() -> EchoApplication.start(config.remotePort(), applicationReady::countDown, applicationShutDown::countDown));
        applicationReady.await();
        Measurements measurements = SourcingConnectionApplication.runPing(config);
        executorService.shutdownNow();
        applicationShutDown.await();
        return measurements;
    }

}