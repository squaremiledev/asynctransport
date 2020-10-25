package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcpacceptance.EchoConnectionApplication;
import dev.squaremile.asynctcpacceptance.SourcingConnectionApplication;

@Disabled
public class RoundTripTimeSeparateAppTest
{

    private static final int PORT = 8889;

    // run as first, when started run, measureRoundTripTime
    @Test
    void runEchoApplication()
    {
        EchoConnectionApplication.main(new String[]{Integer.toString(PORT)});
    }

    @Test
    void measureRoundTripTime()
    {
        final String remoteHost = "localhost";
        final int sendingRatePerSecond = 100_000;
        final int respondToNth = 32;
        final int skippedWarmUpResponses = (sendingRatePerSecond) / respondToNth;
        final int messagesSent = sendingRatePerSecond * 5;
        SourcingConnectionApplication.main(new String[]{
                remoteHost,
                Integer.toString(PORT),
                Integer.toString(sendingRatePerSecond),
                Integer.toString(skippedWarmUpResponses),
                Integer.toString(messagesSent),
                Integer.toString(respondToNth),
                Integer.toString(1)
        });
    }

}
