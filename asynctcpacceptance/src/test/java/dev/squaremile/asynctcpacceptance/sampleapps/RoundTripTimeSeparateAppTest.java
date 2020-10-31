package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcpacceptance.EchoConnectionApplication;
import dev.squaremile.asynctcpacceptance.SourcingConnectionApplication;

@Disabled
public class RoundTripTimeSeparateAppTest
{

    private static final String PORT = Integer.toString(8889);
    private static final String USE_BUFFERS = Integer.toString(1);

    // run as first, when started run, measureRoundTripTime
    @Test
    void runEchoApplication()
    {
        EchoConnectionApplication.main(new String[]{PORT, USE_BUFFERS});
    }

    @Test
    void measureRoundTripTime()
    {
        final String remoteHost = "localhost";
        final int sendingRatePerSecond = 100_000;
        final int respondToNth = 32;
        final int skippedWarmUpResponses = (sendingRatePerSecond * 10) / respondToNth;
        final int messagesSent = sendingRatePerSecond * 60;
        SourcingConnectionApplication.main(new String[]{
                remoteHost,
                PORT,
                Integer.toString(sendingRatePerSecond),
                Integer.toString(skippedWarmUpResponses),
                Integer.toString(messagesSent),
                Integer.toString(respondToNth),
                USE_BUFFERS
        });
    }

}
