package dev.squaremile.trcheck.standalone;

import java.util.concurrent.CountDownLatch;

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