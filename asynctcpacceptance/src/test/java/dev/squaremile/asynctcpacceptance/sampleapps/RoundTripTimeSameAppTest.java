package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.HdrHistogram.Histogram;
import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcpacceptance.demo.ApplicationLifecycle;
import dev.squaremile.asynctcpacceptance.demo.SingleLocalConnectionDemoApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class RoundTripTimeSameAppTest
{
    private static final int WARM_UP = 10_000;
    private static final int TIMES_MEASURED = 100_000;
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

    static Stream<Function<ApplicationFactory, Application>> applicationSuppliers()
    {
        final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
        return Stream.of(
                applicationFactory -> transportApplicationFactory.create(
                        "sameStack",
                        applicationFactory
                ),
                applicationFactory -> transportApplicationFactory.create(
                        "viaRingBuffers",
                        new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 * 1024 + TRAILER_LENGTH])),
                        new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 * 1024 + TRAILER_LENGTH])),
                        applicationFactory
                )
        );
    }

    @ParameterizedTest
    @MethodSource("applicationSuppliers")
    void measureRoundTripTime(final Function<ApplicationFactory, Application> applicationSupplier)
    {
        Application app = applicationSupplier.apply(transport -> new SingleLocalConnectionDemoApplication(
                transport,
                fixedLengthDelineation(2 * 8),
                applicationLifecycle,
                log,
                freePort(),
                (connectionTransport, connectionId) -> new SourcingConnectionApplication(
                        connectionTransport,
                        completeRoundTrips::incrementAndGet,
                        TOTAL,
                        WARM_UP,
                        startedNanos,
                        stoppedNanos,
                        isDone,
                        HISTOGRAM, false
                ),
                EchoConnectionApplication::new
        ));

        app.onStart();

        while (!isDone.get())
        {
            app.work();
        }

        app.onStop();

        while (applicationLifecycle.isUp())
        {
            app.work();
        }

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

}
