package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.Application;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@Disabled
public class RoundTripTimeSeparateAppTest
{
    private static final int WARM_UP = 100_000;
    private static final int TIMES_MEASURED = 1_000_000;
    private static final int TOTAL = WARM_UP + TIMES_MEASURED;
    private static final Histogram HISTOGRAM = new Histogram(TimeUnit.SECONDS.toNanos(10), 3);
    private final MutableBoolean isDone = new MutableBoolean(false);
    private final MutableLong startedNanos = new MutableLong(-1);
    private final MutableLong stoppedNanos = new MutableLong(-1);
    private final MutableLong completeRoundTrips = new MutableLong(0);
    private Application echo;

    @Test
    void measureRoundTripTime()
    {
        int port = freePort(8889);
        MutableBoolean isReady = new MutableBoolean(false);
        Application source = sourceApplication(port, isReady);
//        echo = echoApplication(port);
        echo = noOpApplication(port);

        source.onStart();
        while (!isReady.get())
        {
            source.work();
        }

        echo.onStart();
        while (!isDone.get())
        {
            source.work();
            echo.work();
        }
        echo.onStop();
        source.onStop();

        long messagesExchanged = completeRoundTrips.get() * 2;
        long tookMs = NANOSECONDS.toMillis(stoppedNanos.get() - startedNanos.get());
        long _msgps = messagesExchanged * 1000L / tookMs;

        HISTOGRAM.outputPercentileDistribution(System.out, 1.0);
        System.out.println();
        System.out.print("Exchanged " + messagesExchanged + " messages ");
        System.out.print("at a rate of " + _msgps + " messages per second ");
        System.out.print(" which took " + MILLISECONDS.toSeconds(tookMs) + " seconds");
        System.out.println();
        System.out.println("99.99th percentile is " + HISTOGRAM.getValueAtPercentile(99.99) + " microseconds");
    }

    @Test
    void runSeparateJvmEchoApplication()
    {
        echo = echoApplication(8889);
        echo.onStart();
        while (true)
        {
            echo.work();
        }
    }

    @AfterEach
    void tearDown()
    {
        if (echo != null)
        {
            echo.onStop();
            for (int i = 0; i < 100; i++)
            {
                echo.work();
            }
        }
    }

    private Application noOpApplication(final int port)
    {
        return event ->
        {

        };
    }

    private Application echoApplication(final int port)
    {
        return new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "echo",
                transport -> new ConnectingApplication(
                        transport,
                        port,
                        fixedLengthDelineation(16),
                        EchoConnectionApplication::new
                )
        );
    }

    private Application sourceApplication(final int port, final MutableBoolean isReady)
    {
        return new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "source",
                transport -> new ListeningApplication(
                        transport,
                        fixedLengthDelineation(16),
                        port,
                        () -> isReady.set(true),
                        (connectionTransport, connectionId) -> new SourcingConnectionApplication(
                                connectionTransport,
                                completeRoundTrips::incrementAndGet,
                                TOTAL,
                                WARM_UP,
                                startedNanos,
                                stoppedNanos,
                                isDone,
                                HISTOGRAM
                        )
                )
        );
    }
}
