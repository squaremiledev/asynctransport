package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.HdrHistogram.Histogram;
import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.OnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;
import dev.squaremile.asynctcpacceptance.EchoConnectionApplication;
import dev.squaremile.asynctcpacceptance.SourcingConnectionApplication;
import dev.squaremile.asynctcpacceptance.demo.ApplicationLifecycle;
import dev.squaremile.asynctcpacceptance.demo.SingleLocalConnectionDemoApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class RoundTripTimeDifferentThreadTest
{
    private static final int WARM_UP = 100_000;
    private static final int TIMES_MEASURED = 200_000;
    private static final int TOTAL = WARM_UP + TIMES_MEASURED;
    private static final Histogram HISTOGRAM = new Histogram(TimeUnit.SECONDS.toNanos(10), 3);
    private final ApplicationLifecycle applicationLifecycle = new ApplicationLifecycle();
    private final MutableBoolean isDone = new MutableBoolean(false);
    private final MutableLong startedNanos = new MutableLong(-1);
    private final MutableLong stoppedNanos = new MutableLong(-1);
    private final MutableLong completeRoundTrips = new MutableLong(0);
    private final Consumer<String> log = s ->
    {
    };

    @Test
    @Disabled
    void measureRoundTripTime()
    {
        final OneToOneRingBuffer networkToUser = createBufer();
        final OneToOneRingBuffer userToNetwork = createBufer();
        final ApplicationOnDuty appWithoutTransport = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).createWithoutTransport(
                "viaRingBuffers",
                networkToUser,
                userToNetwork,
                this::createApplication
        );
        final TransportOnDuty transport = new AsyncTcp().transportFactory(NON_PROD_GRADE).create(
                "transport",
                networkToUser,
                userToNetwork
        );
        startBusySpinningSeparateThread(transport);

        appWithoutTransport.onStart();
        while (!isDone.get())
        {
            appWithoutTransport.work();
        }

        appWithoutTransport.onStop();
        while (applicationLifecycle.isUp())
        {
            appWithoutTransport.work();
        }

        // Results
        long messagesExchanged = completeRoundTrips.get() * 2;
        long tookMs = NANOSECONDS.toMillis(stoppedNanos.get() - startedNanos.get());
        long _msgps = messagesExchanged * 1000L / tookMs;

        HISTOGRAM.outputPercentileDistribution(System.out, 1.0);
        System.out.println();
        System.out.print("Exchanged " + messagesExchanged + " messages ");
        System.out.print("at a rate of " + _msgps + " messages per second ");
        System.out.print(" which took " + MILLISECONDS.toSeconds(tookMs) + " seconds");
        System.out.println();
        System.out.println("99th percentile is " + HISTOGRAM.getValueAtPercentile(99.0) + " microseconds");
        System.out.println("99.99th percentile is " + HISTOGRAM.getValueAtPercentile(99.99) + " microseconds");
        assertThat(HISTOGRAM.getValueAtPercentile(99.0)).isLessThan(MILLISECONDS.toMicros(1));
    }

    private SingleLocalConnectionDemoApplication createApplication(final dev.squaremile.asynctcp.transport.api.app.Transport transport)
    {
        return new SingleLocalConnectionDemoApplication(
                transport,
                fixedLengthDelineation(2 * 8),
                applicationLifecycle,
                log,
                freePort(),
                (connectionTransport, connectionId) -> new SourcingConnectionApplication(
                        connectionId,
                        connectionTransport,
                        completeRoundTrips::incrementAndGet,
                        TOTAL,
                        WARM_UP,
                        startedNanos,
                        stoppedNanos,
                        isDone,
                        HISTOGRAM,
                        40_000
                ),
                EchoConnectionApplication::new
        );
    }

    private OneToOneRingBuffer createBufer()
    {
        return new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 * 1024 + TRAILER_LENGTH]));
    }

    private void startBusySpinningSeparateThread(final OnDuty onDuty)
    {
        new Thread(() ->
                   {
                       while (!Thread.interrupted())
                       {
                           onDuty.work();
                       }
                   }
        ).start();
    }
}
