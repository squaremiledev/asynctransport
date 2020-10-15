package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcpacceptance.EchoConnectionApplication;
import dev.squaremile.asynctcpacceptance.SourcingConnectionApplication;

@Disabled
public class RoundTripTimeSeparateAppTest
{

    // run as first, when started run, measureRoundTripTime
    @Test
    void runEchoApplication()
    {
        EchoConnectionApplication.main(new String[]{Integer.toString(8889)});
    }

    @Test
    void measureRoundTripTime()
    {
        String remoteHost = "localhost";
        int remotePort = 8889;
        int sendingRatePerSecond = 48_000;
        int warmUpMessages = 40_000;
        int measuredMessages = 400_000;
        SourcingConnectionApplication.main(new String[]{
                remoteHost,
                Integer.toString(remotePort),
                Integer.toString(sendingRatePerSecond),
                Integer.toString(warmUpMessages),
                Integer.toString(warmUpMessages + measuredMessages)
        });
    }

}
