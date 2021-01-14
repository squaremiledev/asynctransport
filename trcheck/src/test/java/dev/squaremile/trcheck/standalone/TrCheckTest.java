package dev.squaremile.trcheck.standalone;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TrCheckTest
{
    private static final int PORT = 8889;

    // run as first, when started run, measureRoundTripTime
    @Test
    @Disabled
    void runEchoApplication()
    {
        TrCheck.main(new String[]{"ping", "server", String.valueOf(PORT)});
    }

    @Test
    @Disabled
    void measureRoundTripTime()
    {
        final String remoteHost = "localhost";
        final int sendingRatePerSecond = 200000;
        final int respondToNth = 32;
        final int extraDataLength = 0;
        TrCheck.main(new String[]{
                "ping",
                "client",
                remoteHost,
                Integer.toString(PORT),
                Integer.toString(10),
                Integer.toString(5),
                Integer.toString(sendingRatePerSecond),
                Integer.toString(respondToNth),
                Integer.toString(extraDataLength),
                });
    }
}