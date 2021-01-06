package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import dev.squaremile.asynctcpacceptance.EchoApplication;
import dev.squaremile.asynctcpacceptance.SourcingConnectionApplication;
import dev.squaremile.asynctcpacceptance.TimingExtension;

import static dev.squaremile.asynctcp.fixtures.transport.FreePort.freePort;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

@Disabled
@ExtendWith(TimingExtension.class)
public class RoundTripTimeTest
{

    private static final int PORT = 8889;

    @Test
    void shouldExchangeMessages() throws InterruptedException
    {
        int sendingRatePerSecond = 100;
        int respondToNth = 4;
        int secondsRun = 1;
        int secondsWarmUp = 1;
        int messagesSent = sendingRatePerSecond * (secondsWarmUp + secondsRun);
        int skippedWarmUpResponses = (sendingRatePerSecond * secondsWarmUp) / respondToNth;
        exchangeMessages(sendingRatePerSecond, respondToNth, messagesSent, skippedWarmUpResponses);
    }

    @Test
    @Disabled
    void shouldExchangeMessagesAtHighRate() throws InterruptedException
    {
        int sendingRatePerSecond = 10_000;
        int respondToNth = 32;
        int secondsRun = 10;
        int secondsWarmUp = 40;
        int messagesSent = sendingRatePerSecond * (secondsWarmUp + secondsRun);
        int skippedWarmUpResponses = (sendingRatePerSecond * secondsWarmUp) / respondToNth;
        exchangeMessages(sendingRatePerSecond, respondToNth, messagesSent, skippedWarmUpResponses);
    }

    @Test
    @Disabled
    void shouldExchangeMessagesAtHigherRate() throws InterruptedException
    {
        int sendingRatePerSecond = 400_000;
        int respondToNth = 1000;
        int secondsRun = 10;
        int secondsWarmUp = 40;
        int messagesSent = sendingRatePerSecond * (secondsWarmUp + secondsRun);
        int skippedWarmUpResponses = (sendingRatePerSecond * secondsWarmUp) / respondToNth;
        exchangeMessages(sendingRatePerSecond, respondToNth, messagesSent, skippedWarmUpResponses);
    }

    private void exchangeMessages(final int sendingRatePerSecond, final int respondToNth, final int messagesSent, final int skippedWarmUpResponses) throws InterruptedException
    {
        final int port = freePort();
        final CountDownLatch applicationReady = new CountDownLatch(1);
        newSingleThreadExecutor().execute(() -> EchoApplication.start(port, applicationReady::countDown));
        applicationReady.await();

        final String remoteHost = "localhost";
        final int extraDataLength = 64;
        SourcingConnectionApplication.start(
                "",
                remoteHost,
                port,
                sendingRatePerSecond,
                skippedWarmUpResponses,
                messagesSent,
                respondToNth,
                true,
                extraDataLength
        );
    }

    // run as first, when started run, measureRoundTripTime
    @Test
    @Disabled
    void runEchoApplication()
    {
        EchoApplication.start(PORT, () ->
        {

        });
    }

    @Test
    @Disabled
    void measureRoundTripTime()
    {
        final String remoteHost = "localhost";
        final int sendingRatePerSecond = 200000;
        final int respondToNth = 32;
        final int messagesSent = 3000000;
        final int skippedWarmUpResponses = 61250;
        final int extraDataLength = 0;
        SourcingConnectionApplication.main(new String[]{
                remoteHost,
                Integer.toString(9998),
                Integer.toString(sendingRatePerSecond),
                Integer.toString(skippedWarmUpResponses),
                Integer.toString(messagesSent),
                Integer.toString(respondToNth),
                String.valueOf(0),
                Integer.toString(extraDataLength),
                });
    }

}
