package dev.squaremile.trcheck.standalone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TrCheckTest
{
    private static final int PORT = 8889;

    // run first, when started, run measureRoundTripTime
    @Test
    @Disabled
    void runEchoApplication()
    {
        TrCheck.main(new String[]{"benchmark", "server", "-p", String.valueOf(PORT)});
    }

    @Test
    @Disabled
    void measureRoundTripTimeSharedStack()
    {
        measureRoundTripTime();
    }

    @Test
    @Disabled
    void measureRoundTripTimeUsingAeronAndEmbeddedDriver()
    {
        measureRoundTripTime("-ua");
    }

    // run first, when started, run measureRoundTripTimeUsingAeronAndExternalDriver
    @Test
    @Disabled
    void startExternalDriver()
    {
        TrCheck.main(new String[]{"benchmark", "driver", "-d", "/dev/shm/aeron-tcp"});
    }

    @Test
    @Disabled
    void measureRoundTripTimeUsingAeronAndExternalDriver()
    {
        measureRoundTripTime("-ua", "-dd", "/dev/shm/aeron-tcp");
    }

    private void measureRoundTripTime(String... extraArgs)
    {
        final String remoteHost = "localhost";
        final int sendingRatePerSecond = 50000;
        final int respondToNth = 1;
        final int extraDataLength = 0;
        final List<String> baseArgs = Arrays.asList(
                "benchmark",
                "client",
                "-h",
                remoteHost,
                "-p",
                Integer.toString(PORT),
                "-w",
                Integer.toString(20),
                "-t",
                Integer.toString(30),
                "-s",
                Integer.toString(sendingRatePerSecond),
                "-r",
                Integer.toString(respondToNth),
                "-x",
                Integer.toString(extraDataLength)
        );
        final List<String> finalArgsList = new ArrayList<>(baseArgs);
        finalArgsList.addAll(Arrays.asList(extraArgs));
        TrCheck.main(finalArgsList.toArray(new String[0]));
    }
}