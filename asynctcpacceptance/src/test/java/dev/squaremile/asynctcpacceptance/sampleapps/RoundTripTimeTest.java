package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import dev.squaremile.asynctcp.fixtures.TimingExtension;
import dev.squaremile.asynctcpacceptance.EchoApplication;
import dev.squaremile.asynctcpacceptance.SourcingConnectionApplication;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
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
        int sendingRatePerSecond = 100_000;
        int respondToNth = 32;
        int secondsRun = 50;
        int secondsWarmUp = 10;
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
        final int sendingRatePerSecond = 100;
        final int respondToNth = 4;
        final int messagesSent = sendingRatePerSecond * 10;
        final int skippedWarmUpResponses = (sendingRatePerSecond * 5) / respondToNth;
        final int extraDataLength = 1024;
        SourcingConnectionApplication.main(new String[]{
                remoteHost,
                Integer.toString(PORT),
                Integer.toString(sendingRatePerSecond),
                Integer.toString(skippedWarmUpResponses),
                Integer.toString(messagesSent),
                Integer.toString(respondToNth),
                String.valueOf(1),
                Integer.toString(extraDataLength),
                });
    }

}
