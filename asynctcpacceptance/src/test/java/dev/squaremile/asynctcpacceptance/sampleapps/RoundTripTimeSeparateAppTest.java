package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import dev.squaremile.asynctcpacceptance.EchoApplication;
import dev.squaremile.asynctcpacceptance.SourcingConnectionApplication;
import dev.squaremile.asynctcpacceptance.TimingExtension;

@Disabled
@ExtendWith(TimingExtension.class)
public class RoundTripTimeSeparateAppTest
{

    private static final String PORT = Integer.toString(8889);

    // run as first, when started run, measureRoundTripTime
    @Test
    void runEchoApplication()
    {
        EchoApplication.main(new String[]{PORT});
    }

    @Test
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
                PORT,
                Integer.toString(sendingRatePerSecond),
                Integer.toString(skippedWarmUpResponses),
                Integer.toString(messagesSent),
                Integer.toString(respondToNth),
                String.valueOf(1),
                Integer.toString(extraDataLength),
                });
    }

}
