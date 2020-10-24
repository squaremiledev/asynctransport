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
        String remoteHost = "localhost";
        int sendingRatePerSecond = 48_000;
        int warmUpMessages = 48_000 * 10;
        int measuredMessages = 48_000 * 60;
        SourcingConnectionApplication.main(new String[]{
                remoteHost,
                Integer.toString(PORT),
                Integer.toString(sendingRatePerSecond),
                Integer.toString(warmUpMessages),
                Integer.toString(measuredMessages)
        });
    }

}
